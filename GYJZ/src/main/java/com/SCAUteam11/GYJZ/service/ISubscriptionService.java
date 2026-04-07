package com.SCAUteam11.GYJZ.service;

import com.SCAUteam11.GYJZ.DTO.Subscription.SubscriptionVO;
import com.SCAUteam11.GYJZ.entity.mysql.Subscription;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISubscriptionService extends IService<Subscription> {
    boolean subscribe(Long userId, Long projectId);
    boolean unsubscribe(Long userId, Long projectId);
    boolean checkStatus(Long userId, Long projectId);
    List<SubscriptionVO> getMySubscriptions(Long userId);
}