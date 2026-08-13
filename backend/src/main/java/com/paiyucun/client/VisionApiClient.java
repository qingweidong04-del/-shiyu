package com.paiyucun.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.paiyucun.common.BusinessException;
import com.paiyucun.config.AiProperties;
import com.paiyucun.vo.AiRecognitionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;

/**
 * 视觉大模型 API 客户端（OpenAI 兼容协议）
 *
 * 兼容通义千问 Qwen-VL、智谱 GLM-4V、Kimi 等国内视觉模型。
 * 换服务商时通常只需改 app.ai.base-url / api-key / model 配置。
 * 只负责「识别主要景物」，不生成古诗；古诗由 PoemService 从 MySQL 查询。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "vision")
public class VisionApiClient {

    private final AiProperties props;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public VisionApiClient(AiProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(props.getTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(props.getTimeoutMs()));

        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    /**
     * 识别图片中的主要景物
     *
     * @param imageDataUrl 图片 data URL（data:image/xxx;base64,...）
     * @return 识别结果
     */
    public AiRecognitionVO analyze(String imageDataUrl) {
        validateConfig();

        try {
            JsonNode response = restClient.post()
                    .uri(buildUrl())
                    .header("Authorization", "Bearer " + props.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildRequestBody(imageDataUrl))
                    .retrieve()
                    .body(JsonNode.class);
            return parseResponse(response);
        } catch (RestClientResponseException e) {
            String detail = extractErrorDetail(e);
            log.error("视觉模型调用失败 status={} detail={}", e.getStatusCode(), detail, e);
            throw new BusinessException(500, "视觉识别失败：" + detail);
        } catch (ResourceAccessException e) {
            log.error("视觉模型网络异常", e);
            throw new BusinessException(500, "视觉服务连接超时，请稍后重试");
        }
    }

    // ===== 配置校验 =====

    private void validateConfig() {
        if (props.getApiKey() == null || props.getApiKey().isBlank()) {
            throw new BusinessException(500, "未配置视觉 API Key（请设置环境变量 DASHSCOPE_API_KEY）");
        }
        if (props.getModel() == null || props.getModel().isBlank()) {
            throw new BusinessException(500, "未配置视觉模型名（app.ai.model）");
        }
        if (props.getBaseUrl() == null || props.getBaseUrl().isBlank()) {
            throw new BusinessException(500, "未配置视觉接口地址（app.ai.base-url）");
        }
    }

    // ===== 请求构造 =====

    /** OpenAI 兼容地址：{baseUrl}/chat/completions */
    private String buildUrl() {
        return props.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";
    }

    /** 构造 OpenAI 兼容多模态请求体（文字提示 + 图片） */
    private ObjectNode buildRequestBody(String imageDataUrl) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", props.getModel());
        root.put("temperature", 0.1);
        root.put("max_tokens", 64);

        ArrayNode messages = root.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        ArrayNode content = message.putArray("content");
        content.addObject().put("type", "text").put("text", props.getPrompt());
        content.addObject().put("type", "image_url").putObject("image_url").put("url", imageDataUrl);
        return root;
    }

    // ===== 响应解析 =====

    private AiRecognitionVO parseResponse(JsonNode response) {
        if (response == null) {
            throw new BusinessException(500, "视觉模型返回为空");
        }
        String text = response.path("choices").path(0)
                .path("message").path("content").asText("");
        return parseJson(text, objectMapper);
    }

    /**
     * 从模型返回文本中解析出 {objectName, confidence}
     * 兼容：纯 JSON、Markdown 代码块 JSON、前后带解释文字、以及纯文字降级。
     */
    static AiRecognitionVO parseJson(String rawText, ObjectMapper mapper) {
        if (rawText == null || rawText.isBlank()) {
            throw new BusinessException(500, "AI 未识别出景物，请换一张图片试试");
        }
        String text = rawText.trim();
        // 提取首个 { 到最后一个 } 之间的内容，兼容 ```json ... ``` 包裹
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            text = text.substring(start, end + 1);
        }
        try {
            JsonNode obj = mapper.readTree(text);
            String objectName = obj.path("objectName").asText("").trim();
            double confidence = obj.path("confidence").asDouble(0.9);
            if (objectName.isBlank()) {
                throw new BusinessException(500, "AI 返回格式异常（缺少 objectName）");
            }
            if (confidence < 0 || confidence > 1) {
                confidence = 0.9;
            }
            return AiRecognitionVO.of(objectName, confidence);
        } catch (JsonProcessingException e) {
            // 模型未按 JSON 返回时，退化为把整段文本当作景物名
            String cleaned = text.replaceAll("[\\s\"'「」『』，。、！？：；,.!?:;{}\\[\\]]", "");
            if (cleaned.isBlank()) {
                throw new BusinessException(500, "AI 返回格式异常");
            }
            return AiRecognitionVO.of(cleaned, 0.9);
        }
    }

    /** 提取错误信息，便于排查（不包含 API Key） */
    private String extractErrorDetail(RestClientResponseException e) {
        try {
            JsonNode body = objectMapper.readTree(e.getResponseBodyAsString());
            String msg = body.path("error").path("message").asText("");
            if (!msg.isBlank()) {
                return msg.length() > 120 ? msg.substring(0, 120) + "…" : msg;
            }
        } catch (Exception ignored) {
            // 忽略解析失败，使用状态码兜底
        }
        return "HTTP " + e.getStatusCode().value();
    }
}
