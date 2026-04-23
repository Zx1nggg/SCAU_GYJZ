package com.SCAUteam11.GYJZ.config;

import com.SCAUteam11.GYJZ.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import org.springframework.lang.NonNull;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 从请求头中获取 Token (通常放在 Authorization: Bearer <token>)
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7); // 去掉 "Bearer " 前缀
            try {
                // 2. 解析 Token
                Claims claims = jwtUtils.parseToken(token);
                String phone = claims.getSubject();

                // 3. 如果 Token 合法且没有过期，将用户信息存入 Spring Security 的上下文中
                if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(phone, null, new ArrayList<>());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Token 过期或非法，直接放行，后面的 Spring Security 配置会拦截未认证的请求
                logger.error("Token 校验失败: " + e.getMessage());
            }
        }
        // 放行请求，交给下一个过滤器
        filterChain.doFilter(request, response);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // 如果是图片请求，则不运行此 JWT 过滤器
        return path.startsWith("/images/");
    }
}