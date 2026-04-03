package com.SCAUteam11.GYJZ.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@Setter
@Getter
public class StatisticsResponse {
    private Long id;
    private String title;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private Double progressPercentage;
    private Long donorCount; // 捐赠人数
    private Double averageDonation; // 平均捐款
    private Double maxDonation; // 最大捐款
    private Double minDonation; // 最小捐款
    private Long donationCount; // 捐赠次数
}
