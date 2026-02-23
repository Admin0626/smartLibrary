package com.example.smart_library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户角色
     */
    private String role;

    /**
     * 用户角色描述
     */
    private String roleDescription;

    /**
     * 用户状态
     */
    private String status;

    /**
     * 用户状态描述
     */
    private String statusDescription;

    /**
     * 最大借阅数量
     */
    private Integer maxBorrowCount;

    /**
     * 当前借阅数量
     */
    private Integer currentBorrowCount;

    /**
     * 可借阅数量
     */
    private Integer availableBorrowCount;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
