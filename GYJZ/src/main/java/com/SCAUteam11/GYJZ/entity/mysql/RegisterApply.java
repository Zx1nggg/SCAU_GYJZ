package com.SCAUteam11.GYJZ.entity.mysql;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@TableName("register_apply")
@Alias("RegisterApply")
public class RegisterApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String password;
    private String institutionName;
    private String creditCode;
    private String contactPerson;
    private String contactPhone;
    private String qualification; // 资质文件URL
    private String applyReason; // 申请理由
    private Integer applyStatus; // 申请状态 0：待审核 1：审核通过 2：审核不通过
    private String auditRemark; // 审核备注
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 申请时间
    private LocalDateTime auditTime; // 审核时间
    private Long auditorId; // 审核人id
}
