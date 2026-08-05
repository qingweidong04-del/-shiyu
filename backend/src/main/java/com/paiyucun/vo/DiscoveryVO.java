package com.paiyucun.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 发现记录列表视图
 */
@Data
public class DiscoveryVO {

    private Long id;
    private String imageUrl;
    private String objectName;
    private String poemLine;
    private String poemSource;
    private LocalDateTime createTime;
}
