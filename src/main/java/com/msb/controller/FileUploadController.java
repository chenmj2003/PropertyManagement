package com.msb.controller;

import com.msb.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ✨新建✨ 文件上传控制器
 * 统一处理业主头像、车位图片等上传
 * 文件保存到 frontend/public/uploads/ 目录，前端可直接访问
 */
@RestController
@RequestMapping("/api")
public class FileUploadController {

    // 上传目录：项目的 frontend/public/uploads
    private static final String UPLOAD_DIR = System.getProperty("user.dir")
            + File.separator + "frontend" + File.separator + "public" + File.separator + "uploads";

    /**
     * ✨新建✨ 上传文件
     * POST /api/upload
     * 返回 { "url": "/uploads/xxx.jpg" }
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                              HttpServletRequest request) {
        // 校验登录
        String userType = (String) request.getAttribute("userType");
        if (userType == null || (!"owner".equals(userType) && !"admin".equals(userType))) {
            return Result.fail(403, "请先登录");
        }

        // 校验文件不为空
        if (file.isEmpty()) {
            return Result.fail(400, "请选择文件");
        }

        // 校验文件类型（只允许图片）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail(400, "只允许上传图片文件");
        }

        // 校验文件大小（最大 5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.fail(400, "文件大小不能超过 5MB");
        }

        try {
            // 确保上传目录存在
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名（保留原扩展名）
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString().replace("-", "") + extension;

            // 保存文件
            File destFile = new File(uploadDir, newFileName);
            file.transferTo(destFile);

            // 返回访问路径
            String url = "/uploads/" + newFileName;
            Map<String, String> data = new HashMap<>();
            data.put("url", url);

            System.out.println("✅ 文件上传成功: " + url);
            return Result.success("上传成功", data);

        } catch (IOException e) {
            System.err.println("❌ 文件上传失败: " + e.getMessage());
            return Result.fail(500, "上传失败：" + e.getMessage());
        }
    }
}
