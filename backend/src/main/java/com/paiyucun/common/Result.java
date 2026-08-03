package com.paiyucun.common;

import lombok.Data;

/**
 * 统一响应格式
 *
 * @param <T> 数据类型
 */
@Data
public class Result<T> {

    /** 状态码 */
    private int code;

    /** 消息 */
    private String message;

    /** 数据 */
    private T data;

    private Result() {}

    // ===== 工厂方法 =====

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok(String message, T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = message;
        r.data = data;
        return r;
    }

    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }
}
