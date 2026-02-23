package com.example.smart_library.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 借阅记录实体类
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "borrow_records", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_book_id", columnList = "bookId"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_borrow_date", columnList = "borrowDate")
})
public class BorrowRecord {

    /**
     * 借阅记录ID（主键）
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
     * 借阅日期
     */
    @Column(nullable = false)
    private LocalDateTime borrowDate;

    /**
     * 应还日期
     */
    @Column(nullable = false)
    private LocalDateTime dueDate;

    /**
     * 实际归还日期
     */
    private LocalDateTime returnDate;

    /**
     * 续借次数
     */
    @Column(nullable = false)
    private Integer renewCount = 0;

    /**
     * 最大续借次数
     */
    @Column(nullable = false)
    private Integer maxRenewCount = 2;

    /**
     * 是否逾期
     */
    @Column(nullable = false)
    private Boolean isOverdue = false;

    /**
     * 逾期天数
     */
    @Column(nullable = false)
    private Integer overdueDays = 0;

    /**
     * 罚款金额
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal fineAmount = java.math.BigDecimal.ZERO;

    /**
     * 借阅状态：BORROWED-借阅中, RETURNED-已归还, OVERDUE-已逾期, LOST-已丢失
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BorrowStatus status = BorrowStatus.BORROWED;

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
     * 借阅状态枚举
     */
    public enum BorrowStatus {
        BORROWED("借阅中"),
        RETURNED("已归还"),
        OVERDUE("已逾期"),
        LOST("已丢失");

        private final String description;

        BorrowStatus(String description) {
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
        this.borrowDate = now;
        // 默认借阅期限30天
        this.dueDate = now.plusDays(30);
    }

    /**
     * JPA 生命周期回调：更新前设置时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();

        // 检查是否逾期
        if (this.returnDate == null && LocalDateTime.now().isAfter(this.dueDate)) {
            this.isOverdue = true;
            this.overdueDays = (int) java.time.temporal.ChronoUnit.DAYS.between(
                    this.dueDate,
                    LocalDateTime.now()
            );
            // 假设每天罚款0.5元
            this.fineAmount = java.math.BigDecimal.valueOf(this.overdueDays * 0.5);
        }
    }
}
