package com.SCAUteam11.GYJZ.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DonorLoginResponse {
    private Long id;
    private String nickname;
    private Integer role;
    private String phone;
    private String avatar;
    private String token;
    private Integer userStatus;
}
