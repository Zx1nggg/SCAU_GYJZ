package com.SCAUteam11.GYJZ.DTO.User;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserUpdateResponse {
    // 是否更新成功
    private boolean success;

    // 服务器记录的更新时间
//    private LocalDateTime updateTime;
}
