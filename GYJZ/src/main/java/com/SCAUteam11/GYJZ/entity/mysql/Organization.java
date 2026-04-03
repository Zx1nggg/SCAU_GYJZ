package com.SCAUteam11.GYJZ.entity.mysql;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@TableName("organization")
@Alias("Organization")
public class Organization {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code; //机构代码/信用编码
    private String logo; //机构logo
    private String content;// 简介
    private String address; //地址
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private Integer orgStatus;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
