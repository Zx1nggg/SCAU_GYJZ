package com.SCAUteam11.GYJZ.entity.mysql;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@TableName("project")
@Alias("Project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private String title;
    private String content;
    private BigDecimal targetAmount; // 目标金额
    private BigDecimal currentAmount; // 当前金额
    private Integer projectStatus; // 项目状态 1=进行中 2=已结束
    private LocalDateTime startDate; // 开始时间
    private LocalDateTime endDate; // 结束时间
    private String coverImage; // 封面图片
    private String category; // 分类
    private Integer sortOrder; // 排序权重 默认0
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间
    @TableField(exist = false) //表示该字段在数据库中不存在
    private List<Donation> donations; // 一对多
}
