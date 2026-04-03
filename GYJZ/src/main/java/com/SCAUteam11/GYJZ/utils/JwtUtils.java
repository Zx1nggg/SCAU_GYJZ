package com.SCAUteam11.GYJZ.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    // 密钥，必须大于 256 bit（至少 32 个字符）。生产环境则配置在 application.yml 中！
    private static final String SECRET = "SCAUteam11GyjzSecretKeyMustBeLongEnough123456";
    // 过期时间：目前设置 24 小时
    private static final long EXPIRATION = 24 * 60 * 60 * 1000;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // 生成 Token
    public String generateToken(String phone, Long userId) {
        return Jwts.builder()
                .subject(phone) // 通常存手机号或用户名
                .claim("userId", userId) // 存入自定义信息：用户ID
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    // 解析 Token
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}