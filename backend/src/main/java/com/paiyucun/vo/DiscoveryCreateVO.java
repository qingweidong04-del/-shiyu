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
        private String title;
        private String content;
        private String author;
        private String dynasty;
        private String pinyin;
        private String translation;
        private String source;
    }
}
