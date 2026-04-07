package com.SCAUteam11.GYJZ.mapper.mysql;

import com.SCAUteam11.GYJZ.DTO.Subscription.SubscriptionVO;
import com.SCAUteam11.GYJZ.entity.mysql.Subscription;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SubscriptionMapper extends BaseMapper<Subscription> {
    List<SubscriptionVO> selectMySubscriptions(@Param("userId") Long userId);
}