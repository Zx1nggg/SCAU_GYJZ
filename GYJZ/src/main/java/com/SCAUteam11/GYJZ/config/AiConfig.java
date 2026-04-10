package com.SCAUteam11.GYJZ.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.net.http.HttpClient;

@Configuration
public class AiConfig {

    /**
     * 核心修复：拦截 Spring Boot 默认的 HTTP 客户端
     * 强制将协议从 HTTP/2 降级为 HTTP/1.1，完美适配本地大模型（LM Studio / Ollama）
     */
    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return builder -> {
            HttpClient httpClient = HttpClient.newBuilder()
                    // 强制指定使用 HTTP/1.1 协议
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();
            // 替换底层请求工厂
            builder.requestFactory(new JdkClientHttpRequestFactory(httpClient));
        };
    }
}