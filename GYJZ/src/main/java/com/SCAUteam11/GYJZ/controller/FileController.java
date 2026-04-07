package com.SCAUteam11.GYJZ.controller;

import com.SCAUteam11.GYJZ.entity.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
public class FileController {

    /**
     * 通用图片上传接口 (替代原来的 /project 接口)
     * 接口路径：POST /api/v1/upload/{type}
     * 说明：type 会自动决定文件夹名称，如传 logo 就保存到 project-images/logo 下
     */
    @PostMapping("/{type}")
    public Result uploadImage(@PathVariable String type, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("请选择要上传的图片");
        }

        // 安全校验：防止恶意路径穿越
        if (!type.matches("^[a-zA-Z0-9_-]+$")) {
            type = "other";
        }

        // ========== 路径同步逻辑（与 WebMvcConfig 保持高度一致） ==========
        String userDir = System.getProperty("user.dir");
        String folderName = "project-images";
        // 🌟 动态子文件夹名称：根据前端传来的 type 决定
        String subFolder = type;

        // 1. 探测基础路径：优先使用当前目录，不存在则尝试探测父级目录
        File currentDirFolder = new File(userDir, folderName);
        File baseFolder;

        if (currentDirFolder.exists()) {
            baseFolder = currentDirFolder;
        } else {
            // 尝试检测父目录（解决多模块项目 IDE 运行时的路径偏移）
            File parentDirFolder = new File(new File(userDir).getParent(), folderName);
            if (parentDirFolder.exists()) {
                baseFolder = parentDirFolder;
            } else {
                // 如果两处都不存在，则在当前运行目录下创建基础目录
                baseFolder = currentDirFolder;
            }
        }

        // 2. 🌟 创建具体的图片存储子目录 (例如：project-images/logo)
        File targetFolder = new File(baseFolder, subFolder);
        if (!targetFolder.exists()) {
            boolean created = targetFolder.mkdirs();
            if (!created) {
                return Result.fail("无法创建存储目录：" + targetFolder.getAbsolutePath());
            }
        }

        String storagePath = targetFolder.getAbsolutePath() + File.separator;
        // ============================================================

        // 3. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString().replaceAll("-", "") + suffix;

        // 4. 将文件写入磁盘
        try {
            File dest = new File(storagePath + newFilename);
            file.transferTo(dest);

            // 5. 🌟 返回虚拟访问路径，动态拼接 type
            String accessUrl = "/images/" + type + "/" + newFilename;

            System.out.println(">>> [文件上传成功]");
            System.out.println(">>> 物理存储路径: " + dest.getAbsolutePath());
            System.out.println(">>> 数据库保存路径: " + accessUrl);

            return Result.success(accessUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail("服务器图片保存失败：" + e.getMessage());
        }
    }
}