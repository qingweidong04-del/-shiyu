package com.paiyucun.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 保存发现请求
 */
@Data
public class DiscoverySaveDTO {

    @NotBlank(message = "图片地址不能为空")
    private String imageUrl;

    private String objectName;

    private Long poemId;

    private String poemLine;

    private String poemSource;
}
