package com.SCAUteam11.GYJZ.DTO.Donation;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 捐赠记录DTO，传给前端的数据
@Data
public class DonationRecordDTO {
    private Long id;
    private String projectName;
    private BigDecimal amount;
    private LocalDateTime donationTime;
    private String certificateNo;
}
