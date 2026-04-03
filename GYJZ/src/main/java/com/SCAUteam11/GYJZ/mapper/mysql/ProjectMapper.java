package com.SCAUteam11.GYJZ.mapper.mysql;

import com.SCAUteam11.GYJZ.entity.mysql.Project;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.math.BigDecimal;

public interface ProjectMapper extends BaseMapper<Project> {
    Long selectTotalProject(Long orgId); // 根据组织id查询项目总数
    Long selectTotalDonor(Long orgId); // 根据组织id查询捐赠人数
    BigDecimal selectTotalAmount(Long orgId); // 根据组织id查询捐赠总金额
}
