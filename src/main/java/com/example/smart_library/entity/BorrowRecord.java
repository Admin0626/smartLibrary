package com.example.smart_library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_book_id", columnList = "book_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_borrow_date", columnList = "borrowDate"),
        @Index(name = "idx_due_date", columnList = "dueDate")
})
public class BorrowRecord {

    /**
     * 借阅记录ID（主键）
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
     * 借阅状态：BORROWING-借阅中, RETURNED-已归还, OVERDUE-已逾期, LOST-已遗失
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BorrowStatus status = BorrowStatus.BORROWING;

    /**
     * 借阅日期
     */
    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate;

    /**
     * 应还日期
     */
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    /**
     * 实际归还日期
     */
    @Column(name = "return_date")
    private LocalDateTime returnDate;

    /**
     * 逾期天数
     */
    @Column(nullable = false)
    private Integer overdueDays = 0;

    /**
     * 逾期罚金（元）
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal fine = BigDecimal.ZERO;

    /**
     * 是否已支付罚金
     */
    @Column(nullable = false)
    private Boolean finePaid = false;

    /**
     * 借阅次数（累计续借次数）
     */
    @Column(nullable = false)
    private Integer renewCount = 0;

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
        BORROWING("借阅中"),
        RETURNED("已归还"),
        OVERDUE("已逾期"),
        LOST("已遗失");

        private final String description;

        BorrowStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 检查是否逾期
     */
    public boolean isOverdue() {
        return status == BorrowStatus.BORROWING && LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * 计算逾期天数
     */
    public int calculateOverdueDays() {
        if (!isOverdue()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        long days = java.time.temporal.ChronoUnit.DAYS.between(dueDate, now);
        return (int) Math.max(0, days);
    }

    /**
     * 计算罚金（每天1元）
     */
    public BigDecimal calculateFine() {
        int days = calculateOverdueDays();
        return BigDecimal.valueOf(days * 1.0);
    }

    /**
     * 更新逾期状态和罚金
     */
    public void updateOverdueStatus() {
        if (isOverdue()) {
            this.status = BorrowStatus.OVERDUE;
            this.overdueDays = calculateOverdueDays();
            this.fine = calculateFine();
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

        // 设置默认借阅日期和应还日期（默认借阅30天）
        if (this.borrowDate == null) {
            this.borrowDate = now;
        }
        if (this.dueDate == null) {
            this.dueDate = now.plusDays(30);
        }
    }

    /**
     * JPA 生命周期回调：更新前设置时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();

        // 如果归还日期已设置且状态为借阅中，自动更新为已归还
        if (this.returnDate != null && this.status == BorrowStatus.BORROWING) {
            this.status = BorrowStatus.RETURNED;
            // 更新逾期状态
            if (this.returnDate.isAfter(this.dueDate)) {
                this.status = BorrowStatus.OVERDUE;
                this.overdueDays = (int) java.time.temporal.ChronoUnit.DAYS.between(this.dueDate, this.returnDate);
                this.fine = BigDecimal.valueOf(this.overdueDays * 1.0);
            }
        }
    }
}
