package com.SCAUteam11.GYJZ.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean // Bean注解，将方法返回的对象放入Spring容器中
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        // 设置Redis连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 设置键的序列化方式为 String
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // 创建支持 Java 8 时间类型的 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册 Java 8 时间模块（支持 LocalDateTime）
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳，改为使用 ISO-8601 格式（如：2026-03-26T10:45:43）
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 关键：添加类型信息，这样才能正确反序列化
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        // 使用自定义的 ObjectMapper 创建序列化器
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 设置值的序列化方式为 JSON
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);

        return redisTemplate;


    }

}
