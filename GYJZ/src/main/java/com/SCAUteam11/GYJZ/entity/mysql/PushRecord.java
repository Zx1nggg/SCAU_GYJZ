package com.SCAUteam11.GYJZ.entity.mysql;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@TableName("push_record")
@Alias("PushRecord")
public class PushRecord {
    @TableId(type = IdType.AUTO)
    private Long Id;
    private Long userId;
    private Long projectId;
    private String title;
    private String content;
    private Integer type; // 1:进度推送 2:新项目推送
    private LocalDateTime sendTime;
    private Integer sendStatus; // 1:成功 2:失败
}
