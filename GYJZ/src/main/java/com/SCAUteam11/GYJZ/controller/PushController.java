package com.SCAUteam11.GYJZ.controller;

import com.SCAUteam11.GYJZ.entity.Result;
import com.SCAUteam11.GYJZ.entity.mysql.PushRecord;
import com.SCAUteam11.GYJZ.service.impl.PushRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/push")
public class PushController {
    @Autowired
    private PushRecordService pushRecordService;

    /**
     * 获取我的推送消息列表（供前端消息中心调用）
     * * @param userId 用户ID
     * @param page   当前页码（默认1）
     * @param size   每页数量（默认20）
     * @return 分页后的推送记录
     */
    @GetMapping("/my")
    public Result getMyPushList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        // 1. 构建 MyBatis-Plus 的分页对象
        Page<PushRecord> pageParam = new Page<>(page, size);

        // 2. 构建查询条件：查询该用户的记录，并且按发送时间倒序排列（最新的在最上面）
        LambdaQueryWrapper<PushRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PushRecord::getUserId, userId)
                .orderByDesc(PushRecord::getSendTime);

        // 3. 执行查询
        Page<PushRecord> resultPage = pushRecordService.page(pageParam, queryWrapper);

        // 4. 返回统一 Result 格式
        return Result.success(resultPage);
    }
}
