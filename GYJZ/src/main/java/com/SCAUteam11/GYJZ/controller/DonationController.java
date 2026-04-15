package com.SCAUteam11.GYJZ.controller;

import com.SCAUteam11.GYJZ.DTO.Donation.DonationRecordDTO;
import com.SCAUteam11.GYJZ.DTO.Donation.DonorStatisticResponse;
import com.SCAUteam11.GYJZ.entity.Result;
import com.SCAUteam11.GYJZ.entity.mysql.Donation;
import com.SCAUteam11.GYJZ.entity.mysql.Project;
import com.SCAUteam11.GYJZ.service.IDonationService;
import com.SCAUteam11.GYJZ.utils.RedisUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/donations")
public class DonationController {
    @Autowired
    private IDonationService donationService;
    @Autowired
    private  RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RedisUtil redisUtil;

    @GetMapping
    public Result getDonationList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String donorName,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        // 1. 拼接唯一的参数签名并生成 CacheKey
        String paramsStr = String.format("%d-%d-%s-%s-%s-%s-%s-%s-%s",
                page, size, projectId, orgId, donorName, minAmount, maxAmount, startDate, endDate);
        String cacheKey = "donation:list:" + DigestUtils.md5DigestAsHex(paramsStr.getBytes());

        // 2. 尝试从缓存获取
        Object cachedData = redisUtil.get(cacheKey);
        if (cachedData != null) {
            System.out.println("[成功] 命中 Redis 缓存，直接返回！");

            // 【防穿透】判断是否为我们之前缓存的“空标记”
            if ("EMPTY_DATA".equals(cachedData)) {
                return Result.success(new Page<>()); // 返回一个空的 Page 给前端，而不是让它去查库
            }
            return Result.success(cachedData);
        }

        System.out.println("[未命中] 缓存中没有数据，准备查库...");

        // 【防击穿】准备加 Redis 互斥锁（分布式锁）
        // 只有拿到这个锁的线程，才有资格去 MySQL 查数据，其他线程乖乖等
        String lockKey = "lock:" + cacheKey;
        boolean getLock = false;

        try {
            // 尝试加锁，锁的过期时间设置 10 秒（防止线程崩溃导致死锁）
            // 使用 redisTemplate
            Boolean lockResult = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
            getLock = (lockResult != null && lockResult);

            if (getLock) {
                // 拿到锁了！(这里就是经典的 Double Check 双重检查锁)
                // 双重检查锁。因为可能在等锁的时候，前面那个拿到锁的线程已经查完库并把缓存写进去了！
                cachedData = redisUtil.get(cacheKey);
                if (cachedData != null) {
                    return "EMPTY_DATA".equals(cachedData) ? Result.success(new Page<>()) : Result.success(cachedData);
                }

                // 3. 解析日期（拿到锁后才执行这些耗时操作）
                LocalDate start = null;
                LocalDate end = null;
                try {
                    if (startDate != null && !startDate.isEmpty()) start = LocalDate.parse(startDate);
                    if (endDate != null && !endDate.isEmpty()) end = LocalDate.parse(endDate);
                } catch (Exception e) {
                    return Result.fail("日期格式错误");
                }

                // 4. 查询数据库
                System.out.println("[正在查库] 获取到了互斥锁，真正执行 MySQL 查询...");
                Page<Donation> result = donationService.getDonationList(page, size, projectId, orgId, donorName, minAmount, maxAmount, start, end);

                // 5. 将结果写入缓存
                if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
                    // 【防雪崩】基础过期时间 300 秒 + 0~60 秒随机打散！
                    // 这样即使同一时间生成了大量缓存，它们也会在不同的时间点零星过期，不会同时压垮数据库。
                    int randomTTL = 300 + new Random().nextInt(60);
                    redisUtil.set(cacheKey, result, randomTTL);
                    System.out.println("[成功] 数据写入 Redis，设置随机过期时间: " + randomTTL + " 秒");
                } else {
                    // 【防穿透】如果数据库真的什么都没查到！我们存入一个特殊标记，过期时间设置极短 ( 60 秒)
                    // 防止恶意用户拿不存在的条件疯狂请求
                    redisUtil.set(cacheKey, "EMPTY_DATA", 60);
                    System.out.println("[防穿透] 数据库为空，已缓存空标记，60秒内该离谱请求将被直接拦截！");
                }

                return Result.success(result);

            } else {
                // 没有拿到锁的线程怎么办？让其休眠 100 毫秒，然后重新调用自己（重试获取缓存）
                System.out.println("[等待] 未获取到互斥锁，说明有别人正在查库，休眠 100ms 后重试...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // 递归重试
                return getDonationList(page, size, projectId, orgId, donorName, minAmount, maxAmount, startDate, endDate);
            }

        } finally {
            // 无论查询结果如何，最后一定要释放锁！
            if (getLock) {
                redisUtil.del(lockKey);
                System.out.println("[释放锁] 数据处理完毕，互斥锁已释放。");
            }
        }
    }



    @PostMapping
    public Result addDonation(@RequestBody Donation donation) {
        boolean success = donationService.addDonation(donation);
        if (success) {
            clearDonationListCache(); // 清除捐赠列表的缓存
            return Result.success("添加成功");
        } else {
            return Result.fail("添加失败");
        }
    }

    @PutMapping
    public Result updateDonation(@RequestBody Donation donation) {
        boolean success = donationService.updateDonation(donation);
        if (success) {
            clearDonationListCache();
            return Result.success("更新成功");
        } else {
            return Result.fail("更新失败");
        }

    }

    @DeleteMapping("/{id}")
    public Result deleteDonation(@PathVariable Long id) {
        boolean success = donationService.deleteDonation(id);
        if (success) {
            clearDonationListCache();
            return Result.success("删除成功");
        } else {
            return Result.fail("删除失败");
        }
    }

    @GetMapping("/{id}")
    public Result getDonationById(@PathVariable Long id) {
        Donation donation = donationService.getDonationById(id);
        return Result.success(donation);
    }

    @GetMapping("/my")
    public Result getMyDonationList(
            @RequestParam String phone,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = null;
        LocalDate end = null;

        try {
            if (StringUtils.isNotBlank(startDate)) {
                start = LocalDate.parse(startDate);
            }
            if (StringUtils.isNotBlank(endDate)) {
                end = LocalDate.parse(endDate);
            }
        } catch (Exception e) {
            throw new RuntimeException("日期格式错误");
        }

        Page<DonationRecordDTO> result = donationService.getMyDonationList(
                phone, page, size, minAmount, maxAmount, start, end
        );

        return Result.success(result);
    }

    @GetMapping("/statistic/{userId}")
    public Result getDonorStatistic(@PathVariable Long userId) {
        DonorStatisticResponse stats = donationService.getDonorStatistic(userId);
        return Result.success(stats);
    }

    @PostMapping("/create")
    public Result createDonation(@RequestBody Donation donation) {
        // 1. 【接口幂等性】使用 Redis 分布式锁防止“手抖连击”
        // 构造防重 Key：donate:lock:用户ID:项目ID
        String lockKey = "donate:lock:" + donation.getUserId() + ":" + donation.getProjectId();

        // 尝试加锁，锁定 3 秒。
        // 如果返回 false，说明 3 秒内这个用户已经点过一次这个项目的捐赠了
        Boolean getLock = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 3, TimeUnit.SECONDS);
        if (getLock == null || !getLock) {
            return Result.fail("您点击得太快了，系统正在处理您的上一笔捐赠，请稍后再试");
        }

        try {
            // 2. 核心业务落库
            boolean success = donationService.createDonation(donation);

            if (success) {

                // 3 踢掉所有的【项目列表】缓存！(保证用户返回首页立刻看到进度条上涨)
                clearProjectListCache();
                return Result.success("捐赠成功，感谢您的爱心！");
            } else {
                // 如果因为某些业务校验失败（比如项目已结束不能再捐），我们需要手动把锁提前释放掉，让用户可以重新操作
                redisUtil.del(lockKey);
                return Result.fail("捐赠失败，请稍后重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 发生异常也释放锁
            redisUtil.del(lockKey);
            return Result.fail("捐赠异常：" + e.getMessage());
        }
        // 注意：这里的 finally 不写 redisUtil.del(lockKey)。
        // 因为防手抖的锁就是为了让它“卡”满 3 秒钟。如果执行太快，10毫秒就落库成功并释放了锁，
        // 用户的第二次点击依然能钻空子进来。让它自然过期是防连击的最佳实践。
    }
    /**
     * 获取图表动态趋势数据 (近N天捐赠金额)
     * 支持管理员机构隔离
     */
    @GetMapping("/donations-trend")
    public Result getDonationTrend(
            @RequestParam(required = false) Long orgId,
            @RequestParam(defaultValue = "7") int days) {

        List<Map<String, Object>> trendData = donationService.getDonationTrend(orgId, days);
        return Result.success(trendData);
    }

    /**
     * 辅助方法：清理所有带有条件参数的捐赠列表缓存
     */
    private void clearDonationListCache() {
        try {
            Set<String> keys = redisTemplate.keys("donation:list:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                System.out.println("[缓存清理] 捐赠数据变动，清理了 " + keys.size() + " 个捐赠列表缓存！");
            }
        } catch (Exception e) {
            System.err.println("[缓存清理异常] " + e.getMessage());
        }
    }

    /**
     * 辅助方法：清理项目列表缓存
     */
    private void clearProjectListCache() {
        try {
            Set<String> keys = redisTemplate.keys("project:list:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                System.out.println("[资金变动] 用户捐款成功，已联动清理项目大厅列表缓存！");
            }
        } catch (Exception e) {
            System.err.println("[缓存清理异常] " + e.getMessage());
        }
    }

}
