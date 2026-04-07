package com.SCAUteam11.GYJZ.DTO.User;

import lombok.Data;

@Data
public class UserUpdateRequest {
    // 对应前端传来的修改项
    private String nickname;
    private String phone;
    private String avatar;

    // 如果后续需要修改邮箱，可以取消这里的注释
    // private String email;
}