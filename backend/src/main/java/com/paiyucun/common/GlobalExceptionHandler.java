package com.paiyucun.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * 全局异常处理
 *
 * 所有 Controller 抛出的异常在此统一转换为 Result JSON 格式，
 * 无需在 Controller 中写 try-catch。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===== 业务异常 =====

    /** 自定义业务异常 — 直接使用异常的 code 和 message */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: [{}] {}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    // ===== 参数校验 =====

    /** @Validated + @NotBlank 方法参数校验 */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .findFirst()
                .orElse("参数校验失败");
        return Result.fail(400, msg);
    }

    /** Spring Boot 3.x 方法参数校验 */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public Result<Void> handleMethodValidation(HandlerMethodValidationException e) {
        return Result.fail(400, "参数校验失败");
    }

    /** @Valid 请求体校验 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgument(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return Result.fail(400, msg);
    }

    /** 缺少必填参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.fail(400, "缺少参数: " + e.getParameterName());
    }

    // ===== 兜底 =====

    /** 未预期的异常 — 记录日志，不暴露内部错误给客户端 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "服务器内部错误");
    }
}
