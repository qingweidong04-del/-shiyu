package com.paiyucun.util;

import com.paiyucun.common.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传工具类
 */
public class FileUploadUtil {

    /** 允许的图片类型 */
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    /** 允许的后缀 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    /** 最大文件大小 10MB */
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    /**
     * 保存上传的图片
     *
     * @param file       上传文件
     * @param uploadPath 存储根目录
     * @return 相对路径，如 /upload/2026-08-03/uuid.jpg
     */
    public static String saveImage(MultipartFile file, String uploadPath) {
        // ① 空文件校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的图片");
        }

        // ② 大小校验
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(400, "图片大小不能超过 10MB");
        }

        // ③ 类型校验
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(400, "不支持的图片格式，仅支持 JPG/PNG/GIF/WebP");
        }

        // ④ 后缀名校验
        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new BusinessException(400, "文件名不能为空");
        }
        String extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(400, "不支持的图片后缀: " + extension);
        }

        // ⑤ 生成唯一文件名 + 日期子目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String newFilename = UUID.randomUUID().toString() + extension;
        String relativePath = "/upload/" + dateDir + "/" + newFilename;

        // ⑥ 写入磁盘
        try {
            Path targetDir = Paths.get(uploadPath, dateDir);
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(newFilename);
            file.transferTo(targetFile.toFile());
        } catch (IOException e) {
            throw new BusinessException(500, "图片保存失败，请重试");
        }

        return relativePath;
    }
}
