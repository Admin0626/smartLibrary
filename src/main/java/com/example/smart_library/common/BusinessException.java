package com.example.smart_library.common;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     * 1001 - 用户相关
     * 1002 - 密码相关
     * 2001 - 图书相关
     * 2002 - 借阅相关
     * 2003 - 预约相关
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String message;

    /**
     * 构造方法
     * 错误码默认为500
     *
     * @param message 错误信息
     */
    public BusinessException(String message){
        super(message);
        this.code = 500;
        this.message = message;
    }

    /**
     * 构造信息（带错误码和信息）
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(Integer code, String message){
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造信息（带错误码、信息和原始异常）
     * @param code 错误码
     * @param message 错误信息
     * @param cause 原始异常
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}
