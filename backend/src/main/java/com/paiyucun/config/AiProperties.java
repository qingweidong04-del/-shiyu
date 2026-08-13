package com.paiyucun.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 识别配置（OpenAI 兼容协议）
 *
 * 通过 application.yml 的 app.ai.* 配置，api-key 从环境变量读取，绝不硬编码或提交到 Git。
 * 换服务商（通义千问/智谱 GLM/Kimi 等）通常只需改 base-url / api-key / model。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /** 默认提示词：约束模型只返回 JSON 对象，且 objectName 命中诗词库候选词 */
    public static final String DEFAULT_PROMPT =
            "你是「拍遇存」儿童古诗 App 的图像识别模块。请识别图片中最主要的自然景物，" +
            "只返回一个 JSON 对象，不要输出任何解释文字或 Markdown 代码块，格式如下：\n" +
            "{\"objectName\":\"景物名\",\"confidence\":0.95}\n" +
            "objectName 从下列候选词中选择最接近的一个：" +
            "荷花、鸟、花、月亮、草、山、鹅、柳树、鱼、蝴蝶、瀑布、湖、枫叶、太阳、梅花、雪、" +
            "树林、竹子、桃花、树、农田、儿童、节日、船、江水、溪涧、春节。若都不匹配则用「荷花」。" +
            "confidence 为 0 到 1 之间的小数。";

    /** OpenAI 兼容接口地址，如 https://dashscope.aliyuncs.com/compatible-mode/v1 */
    private String baseUrl;

    /** API Key（从环境变量注入） */
    private String apiKey;

    /** 视觉模型名，如 qwen-vl-plus */
    private String model;

    /** 请求超时（毫秒） */
    private int timeoutMs = 30000;

    /** 识别提示词（可覆盖默认值） */
    private String prompt = DEFAULT_PROMPT;
}
