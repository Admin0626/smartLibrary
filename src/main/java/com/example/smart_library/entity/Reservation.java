package com.example.smart_library.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 预约记录实体类
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_book_id", columnList = "bookId"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_expire_time", columnList = "expireTime")
})
public class Reservation {

    /**
     * 预约记录ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID（外键）
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 图书ID（外键）
     */
    @Column(nullable = false)
    private Long bookId;

    /**
     * 预约日期
     */
    @Column(nullable = false)
    private LocalDateTime reservationDate;

    /**
     * 预约过期时间（默认3天后）
     */
    @Column(nullable = false)
    private LocalDateTime expireTime;

    /**
     * 取消日期
     */
    private LocalDateTime cancelDate;

    /**
     * 取消原因
     */
    @Column(length = 500)
    private String cancelReason;

    /**
     * 队列位置（预约队列中的位置）
     */
    @Column(nullable = false)
    private Integer queuePosition = 1;

    /**
     * 通知状态：NOTIFIED-已通知, UNNOTIFIED-未通知
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus notificationStatus = NotificationStatus.UNNOTIFIED;

    /**
     * 通知时间
     */
    private LocalDateTime notifyTime;

    /**
     * 预约状态：PENDING-等待中, ACTIVE-已激活（书可借）, CANCELLED-已取消, COMPLETED-已完成, EXPIRED-已过期
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.PENDING;

    /**
     * 备注
     */
    @Column(length = 500)
    private String remark;

    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 通知状态枚举
     */
    public enum NotificationStatus {
        NOTIFIED("已通知"),
        UNNOTIFIED("未通知");

        private final String description;

        NotificationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 预约状态枚举
     */
    public enum ReservationStatus {
        PENDING("等待中"),
        ACTIVE("已激活"),
        CANCELLED("已取消"),
        COMPLETED("已完成"),
        EXPIRED("已过期");

        private final String description;

        ReservationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * JPA 生命周期回调：创建前设置时间
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.reservationDate = now;
        // 预约有效期3天
        this.expireTime = now.plusDays(3);
    }

    /**
     * JPA 生命周期回调：更新前设置时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
