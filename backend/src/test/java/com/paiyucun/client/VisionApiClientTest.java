package com.paiyucun.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paiyucun.common.BusinessException;
import com.paiyucun.vo.AiRecognitionVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 视觉模型返回结果解析的单元测试（无需真实 API Key 与网络）
 */
class VisionApiClientTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parsePlainJson() {
        AiRecognitionVO vo = VisionApiClient.parseJson("{\"objectName\":\"荷花\",\"confidence\":0.95}", mapper);
        assertNotNull(vo);
        assertEquals("荷花", vo.getObjectName());
        assertEquals(0.95, vo.getConfidence(), 0.0001);
    }

    @Test
    void parseMarkdownFencedJson() {
        String markdown = "```json\n{\"objectName\":\"荷花\",\"confidence\":0.95}\n```";
        AiRecognitionVO vo = VisionApiClient.parseJson(markdown, mapper);
        assertEquals("荷花", vo.getObjectName());
    }

    @Test
    void parseJsonWithSurroundingText() {
        String text = "识别结果是：{\"objectName\":\"荷花\",\"confidence\":0.9}，请查收";
        AiRecognitionVO vo = VisionApiClient.parseJson(text, mapper);
        assertEquals("荷花", vo.getObjectName());
    }

    @Test
    void fallbackToPlainTextWhenNotJson() {
        AiRecognitionVO vo = VisionApiClient.parseJson("荷花", mapper);
        assertEquals("荷花", vo.getObjectName());
    }

    @Test
    void throwWhenTextIsBlank() {
        assertThrows(BusinessException.class, () -> VisionApiClient.parseJson("   ", mapper));
    }
}
