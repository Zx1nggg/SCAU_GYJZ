package com.SCAUteam11.GYJZ.controller;

import com.SCAUteam11.GYJZ.entity.Result;
import com.SCAUteam11.GYJZ.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/common")
public class CommonController {

    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("/sendCode")
    public Result sendVerifyCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            return Result.fail("手机号不能为空");
        }

        String redisKey = "verify:code:" + phone;

        // 1. 防刷机制：如果 Redis 里还有这个手机号的验证码，说明 5 分钟内发过了（或者也可以额外加一个 60 秒防刷键）
        if (redisUtil.get(redisKey) != null) {
            return Result.fail("验证码仍在有效期内，请勿频繁发送");
        }

        // 2. 生成 6 位随机验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 3. 存入 Redis，设置有效期为 2 分钟 (120 秒)
        redisUtil.set(redisKey, code, 120);

        // 4.实际开发中这里会调用第三方 SDK，目前在控制台打印出来，方便测试的时候照着填
        System.out.println("=======================================");
        System.out.println("【模拟短信平台】 发送至手机: " + phone);
        System.out.println("【验证码】: " + code + " (2分钟内有效)");
        System.out.println("=======================================");

        // 绝对不能把 code 放在 Result 里返回给前端！
        return Result.success("验证码发送成功");
    }
}