package com.paiyucun.controller;

import com.paiyucun.common.Result;
import com.paiyucun.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 图片上传接口
 */
@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Value("${app.upload-path:./uploads}")
    private String uploadPath;

    /**
     * 上传图片
     *
     * @param image 图片文件
     * @return { imageUrl: "/upload/2026-08-03/xxx.jpg" }
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("image") MultipartFile image) {
        String imageUrl = FileUploadUtil.saveImage(image, uploadPath);
        return Result.ok(Map.of("imageUrl", imageUrl));
    }
}
