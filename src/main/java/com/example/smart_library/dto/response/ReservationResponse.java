package com.example.smart_library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 预约记录响应DTO
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    /**
     * 预约记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 图书ID
     */
    private Long bookId;

    /**
     * 图书标题
     */
    private String bookTitle;

    /**
     * 图书ISBN
     */
    private String bookIsbn;

    /**
     * 预约日期
     */
    private LocalDateTime reservationDate;

    /**
     * 预约过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 取消日期
     */
    private LocalDateTime cancelDate;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 队列位置
     */
    private Integer queuePosition;

    /**
     * 通知状态
     */
    private String notificationStatus;

    /**
     * 通知状态描述
     */
    private String notificationStatusDescription;

    /**
     * 通知时间
     */
    private LocalDateTime notifyTime;

    /**
     * 预约状态
     */
    private String status;

    /**
     * 预约状态描述
     */
    private String statusDescription;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
