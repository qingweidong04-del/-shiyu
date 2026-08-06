package com.paiyucun.service.impl;

import com.paiyucun.common.BusinessException;
import com.paiyucun.service.AiRecognitionService;
import com.paiyucun.vo.AiRecognitionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * AI 识别模拟实现
 *
 * MVP 阶段随机返回物体名，模拟约 5% 概率失败（验证异常处理链路）。
 * 未来替换真实视觉大模型时：新建实现类，删掉本类 @Service 即可。
 */
@Slf4j
@Service
public class MockAiRecognitionService implements AiRecognitionService {

    private static final List<String> OBJECTS = List.of(
            "荷花", "鸟", "花", "月亮", "草", "山", "鹅", "柳树", "鱼", "蝴蝶",
            "瀑布", "湖", "枫叶", "太阳", "梅花", "雪", "树林", "竹子", "桃花",
            "树", "农田", "儿童", "节日", "船", "江水", "溪涧", "春节"
    );

    private final Random random = new Random();

    @Override
    public AiRecognitionVO recognize(String imageUrl) {
        // 参数校验
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new BusinessException(400, "图片地址不能为空");
        }

        // 模拟 AI 处理延迟
        try {
            Thread.sleep(300 + random.nextInt(500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500, "AI 识别服务被中断");
        }

        // 模拟约 5% 概率识别失败
        if (random.nextInt(100) < 5) {
            log.warn("Mock AI 模拟识别失败: {}", imageUrl);
            throw new BusinessException(500, "AI 识别暂不可用，请稍后重试");
        }

        // 随机返回物体
        String object = OBJECTS.get(random.nextInt(OBJECTS.size()));
        double confidence = 0.75 + random.nextDouble() * 0.20;

        return AiRecognitionVO.of(object, Math.round(confidence * 100.0) / 100.0);
    }
}
