package com.paiyucun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_poem")
public class Poem {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String author;
    private String dynasty;
    private String content;
    private String matchLine;
    private String pinyin;
    private String fullPinyin;
    private String translation;
    private String explanation;
    private String keywords;
    private String objectName;
    private String scene;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
