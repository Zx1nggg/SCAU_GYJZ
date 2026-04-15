package com.SCAUteam11.GYJZ.controller;

import com.SCAUteam11.GYJZ.DTO.OrgStatisticsResponse;
import com.SCAUteam11.GYJZ.DTO.Project.ProjectVO;
import com.SCAUteam11.GYJZ.DTO.StatisticsResponse;
import com.SCAUteam11.GYJZ.entity.Result;
import com.SCAUteam11.GYJZ.entity.mysql.Project;
import com.SCAUteam11.GYJZ.service.IProjectService;
import com.SCAUteam11.GYJZ.utils.RedisUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IProjectService projectService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping
    public Result getProjectList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String category) {

        // 1. 拼接唯一的参数签名并生成 CacheKey
        String paramsStr = String.format("%d-%d-%s-%s-%s-%s-%s-%s-%s",
                page, size, id, orgId, title, content, startDate, endDate, category);
        String cacheKey = "project:list:" + DigestUtils.md5DigestAsHex(paramsStr.getBytes());

        // 2. 尝试从缓存获取
        Object cachedData = redisUtil.get(cacheKey);
        if (cachedData != null) {
            // 【防穿透】拦截无效请求
            if ("EMPTY_DATA".equals(cachedData)) {
                return Result.success(new Page<>());
            }
            return Result.success(cachedData);
        }

        // 【防击穿】准备加互斥锁
        String lockKey = "lock:" + cacheKey;
        boolean getLock = false;

        try {
            // 尝试加锁 (10秒防死锁)
            Boolean lockResult = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
            getLock = (lockResult != null && lockResult);

            if (getLock) {
                // 双重检查
                cachedData = redisUtil.get(cacheKey);
                if (cachedData != null) {
                    return "EMPTY_DATA".equals(cachedData) ? Result.success(new Page<>()) : Result.success(cachedData);
                }

                // 3. String的时间转LocalDate
                LocalDate start = null;
                LocalDate end = null;
                try {
                    if (StringUtils.isNotBlank(startDate)) start = LocalDate.parse(startDate);
                    if (StringUtils.isNotBlank(endDate)) end = LocalDate.parse(endDate);
                } catch (Exception e) {
                    return Result.fail("日期格式错误");
                }

                // 4. 执行 MySQL 查询
                Page<ProjectVO> result = projectService.getProjectList(page, size, id, orgId, title, content, start, end, category);

                // 5. 写入缓存
                if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
                    // 【防雪崩】因为是核心首页，为了保证时效性，基础过期设为 5分钟 (300秒) + 60秒随机
                    int randomTTL = 300 + new Random().nextInt(60);
                    redisUtil.set(cacheKey, result, randomTTL);
                } else {
                    // 【防穿透】空结果缓存 60 秒
                    redisUtil.set(cacheKey, "EMPTY_DATA", 60);
                }

                return Result.success(result);

            } else {
                // 未拿到锁，休眠 100 毫秒后重试
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                return getProjectList(page, size, id, orgId, title, content, startDate, endDate, category);
            }

        } finally {
            if (getLock) {
                redisUtil.del(lockKey);
            }
        }
    }

    @PostMapping
    public Result addProject(@RequestBody Project project) {
        boolean success = projectService.addProject(project);
        if (success) {
            clearProjectListCache();
            return Result.success("添加成功");
        } else {
            return Result.fail("添加失败");
        }
    }

    @PutMapping
    public Result updateProject(@RequestBody Project project) {
        boolean success = projectService.updateProject(project);
        if (success) {
            clearProjectListCache();
            return Result.success("更新成功");
        } else {
            return Result.fail("更新失败");
        }

    }

    @DeleteMapping("/{id}")
    public Result deleteProject(@PathVariable Long id) {
        boolean success = projectService.deleteProject(id);
        if (success) {
            clearProjectListCache();
            return Result.success("删除成功");
        } else {
            return Result.fail("删除失败");
        }
    }

    @GetMapping("/{id}")
    public Result getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        return Result.success(project);
    }

    @GetMapping("/{id}/statistics")
    public Result getProjectStatistics(@PathVariable Long id) {
        StatisticsResponse statistics = projectService.analyseProject(id);
        return Result.success(statistics);
    }

    @GetMapping("/{orgId}/OrgStatistics")
    public Result getOrgProjectStatistics(@PathVariable Long orgId) {
        OrgStatisticsResponse statistics = projectService.analyseOrgProject(orgId);
        return Result.success(statistics);
    }

    /**
     * 管理员手动设置项目状态（例如手动终止、取消、恢复）
     * 接口路径：PUT /api/v1/projects/{id}/status?status=2
     */
    @PutMapping("/projects/{id}/status")
    public Result updateProjectStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        Project project = projectService.getById(id);
        if (project == null) return Result.fail("项目不存在");

        // 逻辑校验：已筹满的项目如果被手动结束是合理的
        project.setProjectStatus(status);

        boolean success = projectService.updateById(project);
        return success ? Result.success("状态更新成功") : Result.fail("状态更新失败");
    }

    /**
     * 辅助方法：清理所有带有条件参数的项目列表缓存
     */
    private void clearProjectListCache() {
        try {
            // 找到 Redis 中所有以 "project:list:" 开头的键
            Set<String> keys = redisTemplate.keys("project:list:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                System.out.println("[缓存清理] 项目数据发生变动，成功清理了 " + keys.size() + " 个旧的项目列表缓存！");
            }
        } catch (Exception e) {
            System.err.println("[缓存清理异常] 清理项目缓存失败：" + e.getMessage());
        }
    }


}
