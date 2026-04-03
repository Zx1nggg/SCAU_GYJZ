package com.SCAUteam11.GYJZ.controller;

import com.SCAUteam11.GYJZ.DTO.AdminLoginResponse;
import com.SCAUteam11.GYJZ.DTO.DonorLoginResponse;
import com.SCAUteam11.GYJZ.entity.Result;
import com.SCAUteam11.GYJZ.entity.mysql.RegisterApply;
import com.SCAUteam11.GYJZ.entity.mysql.User;
import com.SCAUteam11.GYJZ.service.IUserService;
import com.SCAUteam11.GYJZ.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    @Autowired
    private IUserService userService;

    // 注入之前写的 JWT 工具类
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/donor/Login")
    public Result donorLogin(@RequestBody User user){
        // 注意：这里的 userService.donorLogin 内部使用 passwordEncoder.matches() 来比对密文了
        User user1 = userService.donorLogin(user);
        if (user1 != null) {
            // 1. 生成 Token (使用用户的手机号和ID)
            String token = jwtUtils.generateToken(user1.getPhone(), user1.getId());

            DonorLoginResponse data = new DonorLoginResponse();
            data.setId(user1.getId());
            data.setNickname(user1.getNickname());
            data.setRole(user1.getRole());
            data.setPhone(user1.getPhone());
            data.setAvatar(user1.getAvatar());
            data.setUserStatus(user1.getUserStatus());
            // 2. 将 Token 放入返回对象中
            data.setToken(token);

            return Result.success(data);
        }
        return Result.fail("用户名或者密码错误");
    }

    @PostMapping("/admin/Login")
    public Result adminLogin(@RequestBody User user){
        // 注意：这里的 userService.adminLogin 内部应该使用 passwordEncoder.matches() 来比对密文了
        User user1 = userService.adminLogin(user);
        if (user1 != null) {
            // 1. 生成 Token
            String token = jwtUtils.generateToken(user1.getPhone(), user1.getId());

            AdminLoginResponse data = new AdminLoginResponse();
            data.setId(user1.getId());
            data.setUsername(user1.getUsername());
            data.setOrgId(user1.getOrgId());
            data.setNickname(user1.getNickname());
            data.setRole(user1.getRole());
            data.setPhone(user1.getPhone());
            data.setAvatar(user1.getAvatar());
            data.setUserStatus(user1.getUserStatus());
            // 2. 将 Token 放入返回对象中
            data.setToken(token);

            return Result.success(data);
        }
        return Result.fail("用户名或者密码错误");
    }

    @PostMapping("/donor/Register")
    public Result donorRegister(@RequestBody User user){
        // 注意：这里的 userService.donorRegister 内部使用 passwordEncoder.encode() 把明文密码加密后存入数据库
        boolean success = userService.donorRegister(user);
        if (success) {
            return Result.success();
        }else {
            return Result.fail("注册失败");
        }
    }

    @PostMapping("/admin/registerApply")
    public Result submitAdminApply(@RequestBody RegisterApply apply){
        userService.submitAdminApply(apply);
        return Result.success();
    }

    @PostMapping("/admin/approveApply")
    public  Result approveApply(@RequestBody Long applyId,Long auditorId){
        try {
            userService.approveAdminApply(applyId, auditorId);
            return Result.success("审核处理成功");
        } catch (RuntimeException e) {
            // 捕获你在 Service 层抛出的具体业务异常 (如"该手机号已被注册"、"申请不存在")
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("系统异常，审核失败");
        }
    }
}