package com.paiyucun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("discovery")
public class Discovery {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 图片地址 */
    private String imageUrl;

    /** AI 识别物体 */
    private String objectName;

    /** 关联诗词 ID */
    private Long poemId;

    /** 诗句 */
    private String poemLine;

    /** 出处 */
    private String poemSource;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
