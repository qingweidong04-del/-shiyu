package com.paiyucun.common;

import lombok.Getter;

/**
 * 业务异常
 *
 * 用法：
 *   throw new BusinessException("诗词未找到");
 *   throw new BusinessException(404, "诗词未找到");
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 状态码，默认 500 */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
