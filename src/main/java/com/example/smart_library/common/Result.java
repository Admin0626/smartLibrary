package com.example.smart_library.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果封装
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(200)
                .message("操作成功")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(String message) {
        return Result.<T>builder()
                .code(500)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败响应（自定义错误码）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 参数校验失败
     */
    public static <T> Result<T> validationError(String message) {
        return Result.<T>builder()
                .code(400)
                .message("参数校验失败：" + message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 未授权
     */
    public static <T> Result<T> unauthorized(String message) {
        return Result.<T>builder()
                .code(401)
                .message(message != null ? message : "未授权，请先登录")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 禁止访问
     */
    public static <T> Result<T> forbidden(String message) {
        return Result.<T>builder()
                .code(403)
                .message(message != null ? message : "禁止访问")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 资源不存在
     */
    public static <T> Result<T> notFound(String message) {
        return Result.<T>builder()
                .code(404)
                .message(message != null ? message : "资源不存在")
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
