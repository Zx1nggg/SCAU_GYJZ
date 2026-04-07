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
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/donations")
public class DonationController {
    @Autowired
    private IDonationService donationService;

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

        // 1. 拼接唯一的参数签名
        String paramsStr = String.format("%d-%d-%s-%s-%s-%s-%s-%s-%s",
                page, size, projectId, orgId, donorName, minAmount, maxAmount, startDate, endDate);
        String cacheKey = "donation:list:" + DigestUtils.md5DigestAsHex(paramsStr.getBytes());

        System.out.println("👉 当前请求的 Redis Key: " + cacheKey);

        // 2. 尝试从缓存获取
        if (redisUtil.hasKey(cacheKey)) {
            System.out.println("✅ [成功] 命中 Redis 缓存，直接返回！");
            Object cachedData = redisUtil.get(cacheKey);

            // 注意：因为 RedisTemplate 序列化的不同，这里如果直接强转 Page<Donation> 可能会报错
            // 为了安全起见，我们把方法的返回值改成了泛型未指定的 Result，直接塞进去让 SpringMVC 帮你转成 JSON
            return Result.success(cachedData);
        }

        System.out.println("❌ [未命中] 缓存中没有数据，正在查询 MySQL...");

        // 3. 解析日期
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
            return Result.fail("日期格式错误");
        }

        // 4. 查询数据库
        Page<Donation> result = donationService.getDonationList(page, size, projectId, orgId, donorName, minAmount, maxAmount, start, end);

        // 5. 写入缓存 (过期时间 60 秒)
        if (result != null) {
            boolean isSet = redisUtil.set(cacheKey, result, 60);
            if (isSet) {
                System.out.println("💾 [成功] 数据已写入 Redis 缓存，过期时间 60 秒");
            } else {
                System.out.println("⚠️ [失败] 写入 Redis 失败！请检查上方控制台是否有红色序列化报错！");
            }
        }

        return Result.success(result);
    }



    @PostMapping
    public Result addDonation(@RequestBody Donation donation) {
        boolean success = donationService.addDonation(donation);
        if (success) {
            return Result.success("添加成功");
        } else {
            return Result.fail("添加失败");
        }
    }

    @PutMapping
    public Result updateDonation(@RequestBody Donation donation) {
        boolean success = donationService.updateDonation(donation);
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.fail("更新失败");
        }

    }

    @DeleteMapping("/{id}")
    public Result deleteDonation(@PathVariable Long id) {
        boolean success = donationService.deleteDonation(id);
        if (success) {
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
    public  Result createDonation(@RequestBody Donation donation){
        try {
            boolean success = donationService.createDonation(donation);
            if (success) {
                return Result.success("捐赠成功");
            } else {
                return Result.fail("捐赠失败，请稍后重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("捐赠异常：" + e.getMessage());
        }
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

}
