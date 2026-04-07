package com.SCAUteam11.GYJZ.DTO;

import lombok.Data;

@Data
public class PlatformStatsResponse {
    private Long pendingApplyCount; // 待处理的机构申请数
    private Long approvedOrgCount;  // 已入驻机构数
    private Long totalUserCount;    // 全平台注册用户数
}
