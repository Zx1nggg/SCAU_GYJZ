package com.SCAUteam11.GYJZ.DTO.Application;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationVO {
    private Long id;
    private String name;           // 对应 institutionName
    private String code;           // 对应 creditCode
    private String contactPerson;
    private String contactPhone;
    private String content;        // 对应 applyReason (申请理由/简介)
    private Integer orgStatus;     // 对应 applyStatus
    private String auditRemark;
    private String qualification;  // 对应 qualification
    private LocalDateTime createTime;
}