package com.paiyucun.service.impl;

import com.paiyucun.service.AiRecognitionService;
import com.paiyucun.vo.AiRecognitionVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class MockAiRecognitionService implements AiRecognitionService {

    /** 与数据库 t_poem.object_name 保持一致 */
    private static final List<String> OBJECTS = List.of(
            "荷花", "鸟", "花", "月亮", "草", "山", "鹅", "柳树", "鱼", "蝴蝶",
            "瀑布", "湖", "枫叶", "太阳", "梅花", "雪", "树林", "竹子", "桃花",
            "树", "农田", "儿童", "节日", "船", "江水", "溪涧", "春节"
    );

    private final Random random = new Random();

    @Override
    public AiRecognitionVO recognize(String imageUrl) {
        try {
            Thread.sleep(300 + random.nextInt(500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String object = OBJECTS.get(random.nextInt(OBJECTS.size()));
        double confidence = 0.75 + random.nextDouble() * 0.20;

        return AiRecognitionVO.of(object, Math.round(confidence * 100.0) / 100.0);
    }
}
