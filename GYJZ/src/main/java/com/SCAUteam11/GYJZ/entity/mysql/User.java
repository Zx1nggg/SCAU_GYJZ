package com.SCAUteam11.GYJZ.entity.mysql;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import org.apache.ibatis.type.Alias;


import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@TableName("user")
@Alias("User")
public class User {
    @TableId(type= IdType.AUTO)
    private Long id;
    private Long orgId;
    private String username;
    private String password;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer role; //1=捐赠人 2=管理员 9=超级管理员
    private Integer userStatus; // 1=正常 2=禁用
    private String pushToken; // 鸿蒙推送
    private Integer pushEnabled; // 通知许可 1=允许 0=不允许
    @TableField("last_login")
    private LocalDateTime lastLogin;
    @TableField(fill = FieldFill.INSERT) // 插入自动填充
    private LocalDateTime createTime;
}
