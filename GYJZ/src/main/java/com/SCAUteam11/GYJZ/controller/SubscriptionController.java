package com.SCAUteam11.GYJZ.controller;

import com.SCAUteam11.GYJZ.DTO.Subscription.SubscriptionVO;
import com.SCAUteam11.GYJZ.entity.Result;
import com.SCAUteam11.GYJZ.service.ISubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    @Autowired
    private ISubscriptionService subscriptionService;

    // 订阅项目 (前端传入 userId 和 projectId)
    @PostMapping
    public Result subscribe(@RequestBody Map<String, Long> params) {
        Long userId = params.get("userId");
        Long projectId = params.get("projectId");

        if (userId == null || projectId == null) {
            return Result.fail("参数错误：缺失 userId 或 projectId");
        }

        boolean success = subscriptionService.subscribe(userId, projectId);
        return success ? Result.success("订阅成功") : Result.fail("订阅失败");
    }

    // 取消订阅
    @DeleteMapping
    public Result unsubscribe(@RequestParam Long userId, @RequestParam Long projectId) {
        if (userId == null || projectId == null) return Result.fail("参数错误");

        boolean success = subscriptionService.unsubscribe(userId, projectId);
        return success ? Result.success("已取消订阅") : Result.fail("取消订阅失败");
    }

    // 获取我订阅的项目列表
    @GetMapping("/my")
    public Result getMySubscriptions(@RequestParam Long userId) {
        if (userId == null) return Result.fail("参数错误");
        return Result.success(subscriptionService.getMySubscriptions(userId));
    }

    //  查询某个项目的订阅状态 (用于前端显示“已订阅”/“未订阅”按钮)
    @GetMapping("/status")
    public Result checkStatus(@RequestParam Long userId, @RequestParam Long projectId) {
        if (userId == null || projectId == null) return Result.fail("参数错误");
        return Result.success(subscriptionService.checkStatus(userId, projectId));
    }
}
