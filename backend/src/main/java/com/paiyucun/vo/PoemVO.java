package com.paiyucun.vo;

import lombok.Data;

/**
 * 诗词匹配返回视图
 */
@Data
public class PoemVO {

    private String title;
    private String author;
    private String dynasty;
    private String content;
    private String pinyin;
    private String translation;
    private String audioUrl;
}
