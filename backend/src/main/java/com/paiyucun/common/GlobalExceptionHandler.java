package com.paiyucun.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.IOException;

/**
 * 全局异常处理
 *
 * 所有异常统一转为 Result JSON，前端直接展示 message 给用户。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===== 业务异常 =====

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常 [{}] {}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    // ===== 参数校验 =====

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(v -> v.getMessage()).findFirst().orElse("参数校验失败");
        return Result.fail(400, msg);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public Result<Void> handleMethodValidation(HandlerMethodValidationException e) {
        return Result.fail(400, "参数校验失败");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgument(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage()).findFirst().orElse("参数校验失败");
        return Result.fail(400, msg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.fail(400, "缺少必填参数: " + e.getParameterName());
    }

    // ===== 文件上传 =====

    /** 文件大小超限 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return Result.fail(400, "图片大小超过限制（最大 10MB）");
    }

    /** 未选择文件 */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public Result<Void> handleMissingPart(MissingServletRequestPartException e) {
        return Result.fail(400, "请选择要上传的图片");
    }

    /** 文件上传中断/格式异常 */
    @ExceptionHandler(MultipartException.class)
    public Result<Void> handleMultipart(MultipartException e) {
        log.warn("文件上传异常", e);
        return Result.fail(400, "图片上传失败，请重新选择图片");
    }

    // ===== 文件读写 =====

    @ExceptionHandler(IOException.class)
    public Result<Void> handleIOException(IOException e) {
        log.error("文件读写异常", e);
        return Result.fail(500, "文件保存失败，请检查磁盘空间");
    }

    // ===== 数据库 =====

    /** 数据库异常 — 不暴露 SQL 细节 */
    @ExceptionHandler(DataAccessException.class)
    public Result<Void> handleDataAccess(DataAccessException e) {
        log.error("数据库异常", e);
        return Result.fail(500, "数据服务暂不可用，请稍后重试");
    }

    // ===== 兜底 =====

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "服务器繁忙，请稍后重试");
    }
}
