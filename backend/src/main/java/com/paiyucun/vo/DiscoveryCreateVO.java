package com.paiyucun.vo;

import lombok.Data;

/**
 * 完整发现流程返回
 */
@Data
public class DiscoveryCreateVO {

    private Long id;
    private String imageUrl;
    private String objectName;
    private Double confidence;

    /** 匹配的诗词 */
    private PoemInfo poem;

    @Data
    public static class PoemInfo {
        private Long poemId;
        private String title;
        private String content;       // 匹配句
        private String author;
        private String dynasty;
        private String pinyin;        // 匹配句拼音
        private String translation;   // 匹配句翻译
        private String source;        // 出处 "杨万里《小池》"
        private String fullContent;   // 完整诗文
        private String fullPinyin;    // 完整拼音
        private String fullExplanation; // 全文释义
    }
}
