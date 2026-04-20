package com.SCAUteam11.GYJZ.controller;

import com.SCAUteam11.GYJZ.DTO.AdminLoginResponse;
import com.SCAUteam11.GYJZ.DTO.DonorLoginResponse;
import com.SCAUteam11.GYJZ.DTO.User.UserUpdateRequest;
import com.SCAUteam11.GYJZ.DTO.User.UserUpdateResponse;
import com.SCAUteam11.GYJZ.entity.Result;
import com.SCAUteam11.GYJZ.entity.mysql.RegisterApply;
import com.SCAUteam11.GYJZ.entity.mysql.User;
import com.SCAUteam11.GYJZ.service.IUserService;
import com.SCAUteam11.GYJZ.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    @Autowired
    private IUserService userService;

    // 注入之前写的 JWT 工具类
    @Autowired
    private JwtUtils jwtUtils;

    @PutMapping("/users/{userId}/token")
    public Result updatePushToken(@PathVariable Long userId, @RequestBody Map<String, String> params) {
        String pushToken = params.get("pushToken");

        if (pushToken == null || pushToken.trim().isEmpty()) {
            return Result.fail("Push Token 不能为空");
        }

        User user = userService.getById(userId);
        if (user != null) {
            user.setPushToken(pushToken);
            userService.updateById(user);
            System.out.println("[设备绑定] 用户 ID: " + userId + " 已绑定鸿蒙 Token: " + pushToken);
            return Result.success("设备绑定成功");
        }

        return Result.fail("找不到该用户");
    }

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
            data.setCreateTime(String.valueOf(user1.getCreateTime()));
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
    // NOTE 相关的方法已经迁移到了SAdminController并合并为一个auditRegisterApply，故下面方法废除

    @PostMapping("/admin/registerApply")
    public Result submitAdminApply(@RequestBody RegisterApply apply){
        userService.submitAdminApply(apply);
        return Result.success();
    }
//
//
//
//    @PostMapping("/admin/approveApply")
//    public  Result approveApply(@RequestBody Long applyId,Long auditorId){
//        try {
//            userService.approveAdminApply(applyId, auditorId);
//            return Result.success("审核处理成功");
//        } catch (RuntimeException e) {
//            // 捕获你在 Service 层抛出的具体业务异常 (如"该手机号已被注册"、"申请不存在")
//            return Result.fail(e.getMessage());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return Result.fail("系统异常，审核失败");
//        }
//    }
    /**
     * 统一的用户资料更新接口（捐赠人、机构管理员通用）
     * 路径：PUT /api/v1/users/{userId}
     * 前端通过 authStore 拿到 userId 直接拼在路径上
     */
    @PutMapping("/users/{userId}")
    public Result updateUser(@PathVariable Long userId, @RequestBody UserUpdateRequest request) {
        try {
            // 将更新逻辑全权交由 Service 处理
            UserUpdateResponse data = userService.updateUser(userId, request);
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("系统繁忙，更新失败");
        }
    }

    // 管理员修改密码
    @PutMapping("/users/{userId}/password")
    public Result updatePassword(@PathVariable Long userId, @RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        // 1. 基础非空校验
        if (!StringUtils.hasLength(oldPassword) || !StringUtils.hasLength(newPassword)) {
            return Result.fail("原密码或新密码不能为空");
        }

        // 2. 拦截长度或格式
        if (newPassword.length() < 6) {
            return Result.fail("新密码长度不能少于6位");
        }

        try {
            // 3. 调用 Service 层核心逻辑
            userService.updatePassword(userId, oldPassword, newPassword);
            return Result.success("密码修改成功");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("系统异常，修改失败");
        }
    }

    @PutMapping("/users/{userId}/reset-password")
    public Result resetPassword(@PathVariable Long userId) {
        try {
            // 调用 Service 层核心逻辑
            userService.resetPassword(userId);
            return Result.success("密码已成功重置为默认密码");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("系统异常，重置失败");
        }
    }
}