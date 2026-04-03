package com.SCAUteam11.GYJZ.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 获取当前工作目录
        String userDir = System.getProperty("user.dir");
        String folderName = "project-images";

        // 2. 智能路径检测
        // 先检查当前目录下的 project-images
        File currentDirFolder = new File(userDir, folderName);
        String finalPath;

        if (currentDirFolder.exists()) {
            finalPath = currentDirFolder.getAbsolutePath() + File.separator;
        } else {
            // 如果找不到，尝试检查上一级目录 (解决多级 Maven 项目路径偏移问题)
            File parentDirFolder = new File(new File(userDir).getParent(), folderName);
            if (parentDirFolder.exists()) {
                finalPath = parentDirFolder.getAbsolutePath() + File.separator;
            } else {
                // 默认回退到当前目录（虽然不存在，但打印警告供调试）
                finalPath = currentDirFolder.getAbsolutePath() + File.separator;
            }
        }

        // 3. 调试输出
        System.out.println("=========================================");
        System.out.println(">>> [静态资源映射 - 智能检测]");
        System.out.println(">>> 运行目录: " + userDir);

        File checkFile = new File(finalPath);
        if (!checkFile.exists()) {
            System.err.println(">>> [警告] 依然找不到图片目录！");
            System.err.println(">>> 请确保在以下位置创建了文件夹: " + finalPath);
        } else {
            System.out.println(">>> [成功] 已自动定位到图片目录: " + finalPath);
        }

        /**
         * 映射规则：
         * 访问 /images/project/xxx.png
         * 对应磁盘：finalPath + project/xxx.png
         */
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + finalPath);

        System.out.println(">>> 映射规则: /images/** -> " + finalPath);
        System.out.println("=========================================");
    }
}