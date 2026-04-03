package com.SCAUteam11.GYJZ.DTO.Donation;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DonorStatisticResponse {
    private BigDecimal totalAmount;   // 总金额
    private Integer totalCount;       // 捐赠次数
    private Integer projectCount;     // 支持项目数
}
