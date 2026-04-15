package com.SCAUteam11.GYJZ.service.impl;

import com.SCAUteam11.GYJZ.DTO.Subscription.SubscriptionVO;
import com.SCAUteam11.GYJZ.entity.mysql.Subscription;
import com.SCAUteam11.GYJZ.mapper.mysql.SubscriptionMapper;
import com.SCAUteam11.GYJZ.service.ISubscriptionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionServiceImpl extends ServiceImpl<SubscriptionMapper, Subscription> implements ISubscriptionService {

    @Override
    public boolean subscribe(Long userId, Long projectId) {
        // 防止重复订阅：先检查是否存在
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<Subscription>()
                .eq(Subscription::getUserId, userId)
                .eq(Subscription::getProjectId, projectId));

        if (count > 0) return true; // 已经订阅过了

        Subscription sub = new Subscription();
        sub.setUserId(userId);
        sub.setProjectId(projectId);
        sub.setCreateTime(LocalDateTime.now());
        return baseMapper.insert(sub) > 0;
    }

    @Override
    public boolean unsubscribe(Long userId, Long projectId) {
        return baseMapper.delete(new LambdaQueryWrapper<Subscription>()
                .eq(Subscription::getUserId, userId)
                .eq(Subscription::getProjectId, projectId)) > 0;
    }

    @Override
    public boolean checkStatus(Long userId, Long projectId) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<Subscription>()
                .eq(Subscription::getUserId, userId)
                .eq(Subscription::getProjectId, projectId));
        return count > 0;
    }

    @Override
    public List<SubscriptionVO> getMySubscriptions(Long userId) {
        // 调用 Mapper 中的 SQL
        return baseMapper.selectMySubscriptions(userId);
    }
}