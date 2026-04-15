package com.SCAUteam11.GYJZ.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@Setter
@Getter
// 每个机构的统计数据
public class OrgStatisticsResponse {
    private Long orgId;
    private Long totalProjectCount; // 总项目数
    private BigDecimal totalAmount; // 总金额
    private Long totalDonorCount; // 总捐赠人数
    private Long totalDonationCount; // 总捐赠次数
}
