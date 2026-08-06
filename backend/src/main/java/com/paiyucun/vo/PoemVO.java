package com.paiyucun.vo;

import lombok.Data;

@Data
public class PoemVO {

    private Long poemId;
    private String title;
    private String author;
    private String dynasty;
    private String content;        // 匹配句
    private String pinyin;         // 匹配句拼音
    private String translation;
    private String audioUrl;
    private String fullContent;    // 完整诗文
    private String fullPinyin;     // 完整拼音
    private String fullExplanation; // 全文释义
}
