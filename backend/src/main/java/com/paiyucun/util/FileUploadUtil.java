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
 *
 * 校验链：空文件 → 大小 → Content-Type → 后缀名 → 写入磁盘
 */
public class FileUploadUtil {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    /** 最大 10MB */
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    /**
     * 保存上传图片，返回访问路径
     *
     * @param file       上传文件
     * @param uploadPath 存储根目录
     * @return 相对路径，如 /upload/2026-08-06/uuid.jpg
     */
    public static String saveImage(MultipartFile file, String uploadPath) {
        validateFile(file);
        String relativePath = buildPath(file);
        writeToDisk(file, uploadPath, relativePath);
        return relativePath;
    }

    // ===== 校验 =====

    private static void validateFile(MultipartFile file) {
        // ① 空文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的图片");
        }

        // ② 大小超限
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(400, "图片大小不能超过 10MB，当前 " + formatSize(file.getSize()));
        }

        // ③ Content-Type 校验
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(400, "不支持的图片格式（" + contentType + "），仅支持 JPG / PNG / GIF / WebP");
        }

        // ④ 后缀名校验
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new BusinessException(400, "文件名无效");
        }
        String extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(400, "不支持的文件后缀（" + extension + "），仅支持 " + ALLOWED_EXTENSIONS);
        }
    }

    // ===== 路径生成 =====

    private static String buildPath(MultipartFile file) {
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")).toLowerCase();
        return "/upload/" + dateDir + "/" + UUID.randomUUID() + ext;
    }

    // ===== 写入 =====

    private static void writeToDisk(MultipartFile file, String uploadPath, String relativePath) {
        try {
            Path targetFile = Paths.get(uploadPath, relativePath.replace("/upload/", ""));
            Files.createDirectories(targetFile.getParent());
            file.transferTo(targetFile);
        } catch (IOException e) {
            throw new BusinessException(500, "图片保存失败，磁盘空间不足或目录无写入权限");
        }
    }

    // ===== 工具 =====

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024 * 1024) return String.format("%.1fKB", bytes / 1024.0);
        return String.format("%.1fMB", bytes / (1024.0 * 1024.0));
    }
}
