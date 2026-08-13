package com.paiyucun.service.impl;

import com.paiyucun.client.VisionApiClient;
import com.paiyucun.common.BusinessException;
import com.paiyucun.service.AiRecognitionService;
import com.paiyucun.util.ImageUtil;
import com.paiyucun.vo.AiRecognitionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * 真实视觉模型识别实现
 *
 * 流程：图片地址 → 读取本地图片 → 压缩 → base64 data URL → VisionApiClient → AiRecognitionVO。
 * 仅当 app.ai.provider=vision 时启用（与 MockAiRecognitionService 互斥）。
 * 不保存任何临时图片，压缩在内存中完成。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "vision")
public class VisionAiRecognitionService implements AiRecognitionService {

    private final VisionApiClient visionApiClient;

    @Value("${app.upload-path:./uploads}")
    private String uploadPath;

    public VisionAiRecognitionService(VisionApiClient visionApiClient) {
        this.visionApiClient = visionApiClient;
    }

    @Override
    public AiRecognitionVO recognize(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new BusinessException(400, "图片地址不能为空");
        }

        byte[] original = readImage(imageUrl);
        String mimeType = detectMime(imageUrl);

        // 压缩以控制流量与成本；失败或无法解码（如 WebP）则按原图发送
        byte[] toSend = original;
        String sendMime = mimeType;
        byte[] compressed = ImageUtil.compressToJpeg(original, ImageUtil.DEFAULT_MAX_DIMENSION);
        if (compressed != null && compressed.length < original.length) {
            toSend = compressed;
            sendMime = "image/jpeg";
        }

        String dataUrl = "data:" + sendMime + ";base64," + Base64.getEncoder().encodeToString(toSend);
        AiRecognitionVO result = visionApiClient.analyze(dataUrl);
        log.info("视觉识别结果: {} -> {}（置信度 {}）", imageUrl, result.getObjectName(), result.getConfidence());
        return result;
    }

    /** 读取本地图片字节 */
    private byte[] readImage(String imageUrl) {
        try {
            // imageUrl 形如 /upload/2026-08-13/uuid.jpg，与 FileUploadUtil 的写入路径约定一致
            Path file = Paths.get(uploadPath, imageUrl.replace("/upload/", ""));
            return Files.readAllBytes(file);
        } catch (IOException e) {
            log.error("读取图片文件失败: {}", imageUrl, e);
            throw new BusinessException(500, "读取图片失败，请重新上传");
        }
    }

    /** 根据后缀推断 MIME 类型 */
    private String detectMime(String imageUrl) {
        String lower = imageUrl.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}
