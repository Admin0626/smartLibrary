package com.example.smart_library.common;

import lombok.Getter;

/**
 * 业务异常类
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 默认错误码：500
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    /**
     * 自定义错误码
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造异常（带原因）
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
        this.message = message;
    }

    /**
     * 构造异常（带错误码和原因）
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 用户不存在异常
     */
    public static BusinessException userNotFound() {
        return new BusinessException(1001, "用户不存在");
    }

    /**
     * 用户已存在异常
     */
    public static BusinessException userAlreadyExists(String username) {
        return new BusinessException(1002, "用户 " + username + " 已存在");
    }

    /**
     * 密码错误异常
     */
    public static BusinessException passwordError() {
        return new BusinessException(1003, "用户名或密码错误");
    }

    /**
     * 图书不存在异常
     */
    public static BusinessException bookNotFound() {
        return new BusinessException(2001, "图书不存在");
    }

    /**
     * 图书不可借异常
     */
    public static BusinessException bookNotAvailable() {
        return new BusinessException(2002, "该图书暂无可借数量");
    }

    /**
     * 借阅数量超限异常
     */
    public static BusinessException borrowLimitExceeded() {
        return new BusinessException(3001, "已达到最大借阅数量");
    }

    /**
     * 借阅记录不存在异常
     */
    public static BusinessException borrowRecordNotFound() {
        return new BusinessException(3002, "借阅记录不存在");
    }

    /**
     * 预约记录不存在异常
     */
    public static BusinessException reservationNotFound() {
        return new BusinessException(4001, "预约记录不存在");
    }

    /**
     * 重复预约异常
     */
    public static BusinessException duplicateReservation() {
        return new BusinessException(4002, "您已经预约了这本书，请勿重复预约");
    }

    /**
     * 未授权异常
     */
    public static BusinessException unauthorized() {
        return new BusinessException(401, "未授权，请先登录");
    }

    /**
     * 无权限异常
     */
    public static BusinessException forbidden() {
        return new BusinessException(403, "无权限访问");
    }
}
