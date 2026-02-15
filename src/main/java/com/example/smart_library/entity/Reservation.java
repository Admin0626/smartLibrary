package com.example.smart_library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_book_id", columnList = "book_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "createdAt"),
        @Index(name = "idx_notify_time", columnList = "notifyTime")
})
public class Reservation {

    /**
     * 预约记录ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 用户名（冗余字段，便于查询）
     */
    @Column(name = "user_name", length = 50)
    private String userName;

    /**
     * 用户联系方式（手机号）
     */
    @Column(name = "user_phone", length = 20)
    private String userPhone;

    /**
     * 图书ID
     */
    @NotNull(message = "图书ID不能为空")
    @Column(name = "book_id", nullable = false)
    private Long bookId;

    /**
     * 图书信息（冗余字段，便于查询）
     */
    @Column(name = "book_isbn", length = 20)
    private String bookIsbn;

    @Column(name = "book_title", length = 200)
    private String bookTitle;

    /**
     * 预约状态：PENDING-待处理, CONFIRMED-已确认, COMPLETED-已完成, CANCELLED-已取消, EXPIRED-已过期
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.PENDING;

    /**
     * 预约时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime reservationTime;

    /**
     * 预约类型：WAITING_LIST-排队预约, SPECIFIC_TIME-指定时间
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationType reservationType = ReservationType.WAITING_LIST;

    /**
     * 预约取书时间（如果是指定时间预约）
     */
    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;

    /**
     * 通知时间（图书可借时的通知时间）
     */
    @Column(name = "notify_time")
    private LocalDateTime notifyTime;

    /**
     * 确认时间
     */
    @Column(name = "confirm_time")
    private LocalDateTime confirmTime;

    /**
     * 完成时间
     */
    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    /**
     * 取消时间
     */
    @Column(name = "cancel_time")
    private LocalDateTime cancelTime;

    /**
     * 预约序号（排队位置）
     */
    @Column(nullable = false)
    private Integer queueNumber = 0;

    /**
     * 是否已通知
     */
    @Column(nullable = false)
    private Boolean notified = false;

    /**
     * 有效期天数（预约后多少天内有效）
     */
    @Column(nullable = false)
    private Integer validDays = 7;

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
     * 预约状态枚举
     */
    public enum ReservationStatus {
        PENDING("待处理"),
        CONFIRMED("已确认"),
        COMPLETED("已完成"),
        CANCELLED("已取消"),
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
     * 预约类型枚举
     */
    public enum ReservationType {
        WAITING_LIST("排队预约"),
        SPECIFIC_TIME("指定时间");

        private final String description;

        ReservationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 检查预约是否有效（在有效期内）
     */
    public boolean isValid() {
        if (status != ReservationStatus.PENDING) {
            return false;
        }

        LocalDateTime expiryTime = reservationTime.plusDays(validDays);
        return LocalDateTime.now().isBefore(expiryTime);
    }

    /**
     * 检查预约是否已过期
     */
    public boolean isExpired() {
        if (status != ReservationStatus.PENDING) {
            return false;
        }

        LocalDateTime expiryTime = reservationTime.plusDays(validDays);
        return LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * 确认预约
     */
    public void confirm() {
        if (status == ReservationStatus.PENDING && isValid()) {
            this.status = ReservationStatus.CONFIRMED;
            this.confirmTime = LocalDateTime.now();
            this.notified = true;
        }
    }

    /**
     * 取消预约
     */
    public void cancel() {
        if (status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED) {
            this.status = ReservationStatus.CANCELLED;
            this.cancelTime = LocalDateTime.now();
        }
    }

    /**
     * 标记为已完成
     */
    public void complete() {
        if (status == ReservationStatus.CONFIRMED) {
            this.status = ReservationStatus.COMPLETED;
            this.completeTime = LocalDateTime.now();
        }
    }

    /**
     * 标记为已过期
     */
    public void expire() {
        if (status == ReservationStatus.PENDING) {
            this.status = ReservationStatus.EXPIRED;
        }
    }

    /**
     * 发送通知
     */
    public void sendNotification() {
        if (status == ReservationStatus.PENDING && !this.notified) {
            this.notifyTime = LocalDateTime.now();
            this.notified = true;
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

        // 设置默认预约时间
        if (this.reservationTime == null) {
            this.reservationTime = now;
        }
    }

    /**
     * JPA 生命周期回调：更新前设置时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();

        // 检查是否过期
        if (status == ReservationStatus.PENDING && isExpired()) {
            this.status = ReservationStatus.EXPIRED;
        }
    }
}
