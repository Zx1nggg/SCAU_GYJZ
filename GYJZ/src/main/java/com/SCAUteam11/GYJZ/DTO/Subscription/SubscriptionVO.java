package com.SCAUteam11.GYJZ.DTO.Subscription;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubscriptionVO {
    private Long id;
    private Long projectId;
    private String projectTitle;
    private Integer projectStatus;
    private LocalDateTime createTime;
}