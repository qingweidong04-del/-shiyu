package com.paiyucun.service.impl;

import com.paiyucun.service.AiRecognitionService;
import com.paiyucun.vo.AiRecognitionVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * AI 识别模拟实现
 *
 * MVP 阶段随机返回预置物体名。
 * 未来替换为真实视觉大模型（如 OpenAI Vision / 百度文心）：
 *   新建 OpenAiRecognitionService 实现 AiRecognitionService 接口，
 *   将本类的 @Service 注解去掉即可。
 */
@Service
public class MockAiRecognitionService implements AiRecognitionService {

    private static final List<String> OBJECTS = List.of(
            "荷花", "鸟", "花", "月亮", "草", "山", "鹅", "柳树", "鱼", "蝴蝶"
    );

    private final Random random = new Random();

    @Override
    public AiRecognitionVO recognize(String imageUrl) {
        // 模拟 AI 处理延迟
        try {
            Thread.sleep(300 + random.nextInt(700));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 随机返回一个物体 + 模拟置信度
        String object = OBJECTS.get(random.nextInt(OBJECTS.size()));
        double confidence = 0.75 + random.nextDouble() * 0.20; // 0.75 ~ 0.95

        return AiRecognitionVO.of(object, Math.round(confidence * 100.0) / 100.0);
    }
}
