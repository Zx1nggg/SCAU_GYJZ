package com.SCAUteam11.GYJZ.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    /**
     * 注册 MyBatis-Plus 分页拦截器
     * 没有这个 Bean，所有带有 Page 参数的查询都会变成全表查询！
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 检查 打印
        System.out.println("=========================================");
        System.out.println("====== MyBatis-Plus 分页插件已成功加载 ======");
        System.out.println("=========================================");
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 指定数据库类型为 MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}