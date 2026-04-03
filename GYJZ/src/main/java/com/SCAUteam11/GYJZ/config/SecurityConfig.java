package com.SCAUteam11.GYJZ.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                // 1. 关闭 CSRF 防护（前后端分离项目不需要）
                .csrf(AbstractHttpConfigurer::disable)
                // 2. 禁用 Session（我们用 JWT，属于无状态）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 3. 配置路径拦截规则
                .authorizeHttpRequests(auth -> auth
                        // 图片路径也放行了
                        .requestMatchers("/images/**").permitAll()
                        // 下面这些路径放行（不登录也能访问），比如登录、注册、查看公开的项目列表等
                        .requestMatchers("/api/v1/donor/Login","/api/v1/donor/Register","/api/v1/admin/Login","/api/v1/admin/registerApply").permitAll()
                        // 鸿蒙 App 即使没有 Token 也能正常浏览公益项目
                        .requestMatchers(HttpMethod.GET, "/api/v1/projects", "/api/v1/projects/**").permitAll()
                        // Swagger 接口文档也放行（使用了 springdoc）
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // 除此之外的所有请求，都必须登录后才能访问
                        .anyRequest().authenticated()
                )
                // 4. 将 JWT 过滤器放在用户名密码认证过滤器之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}