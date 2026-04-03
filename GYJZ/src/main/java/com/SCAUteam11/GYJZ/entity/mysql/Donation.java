package com.SCAUteam11.GYJZ.entity.mysql;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@TableName("donation")
@Alias("Donation")
public class Donation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long userId;
    private String donorName;
    private String donorPhone;
    private BigDecimal amount; //捐赠金额
    private LocalDateTime donationTime; //捐赠时间
    private String certificateNo; // 凭证号
    private String payment;
    private Integer source; // 捐赠来源 1:线上 2:线下
    private Integer donationStatus;
    private LocalDateTime refundTime;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    // 用于连表查询时接收项目名称，不映射到 donation 表
    @TableField(exist = false)
    private String projectTitle;
}
