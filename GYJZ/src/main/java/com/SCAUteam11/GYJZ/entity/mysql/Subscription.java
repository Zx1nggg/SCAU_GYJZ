package com.SCAUteam11.GYJZ.entity.mysql;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@TableName("subscription")
@Alias("Subscription")
public class Subscription {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long projectId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
