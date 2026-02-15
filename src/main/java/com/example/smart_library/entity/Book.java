package com.example.smart_library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 图书实体类
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books", indexes = {
        @Index(name = "idx_isbn", columnList = "isbn"),
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_author", columnList = "author"),
        @Index(name = "idx_status", columnList = "status")
})
public class Book {

    /**
     * 图书ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 图书ISBN（唯一）
     */
    @NotBlank(message = "ISBN不能为空")
    @Column(unique = true, nullable = false, length = 20)
    private String isbn;

    /**
     * 书名
     */
    @NotBlank(message = "书名不能为空")
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 作者
     */
    @NotBlank(message = "作者不能为空")
    @Column(nullable = false, length = 100)
    private String author;

    /**
     * 出版社
     */
    @NotBlank(message = "出版社不能为空")
    @Column(nullable = false, length = 100)
    private String publisher;

    /**
     * 出版日期
     */
    @Column
    private LocalDateTime publishDate;

    /**
     * 图书分类
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BookCategory category;

    /**
     * 图书价格
     */
    @DecimalMin(value = "0.00", message = "价格不能为负数")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * 总库存数量
     */
    @NotNull(message = "总库存数量不能为空")
    @Column(nullable = false)
    private Integer totalStock = 0;

    /**
     * 当前可借数量
     */
    @NotNull(message = "当前可借数量不能为空")
    @Column(nullable = false)
    private Integer availableStock = 0;

    /**
     * 借出数量
     */
    @Column(nullable = false)
    private Integer borrowedCount = 0;

    /**
     * 图书状态：AVAILABLE-可借, BORROWED-已借出, RESERVED-已预约, LOST-遗失, DAMAGED-损坏, MAINTENANCE-维护中
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookStatus status = BookStatus.AVAILABLE;

    /**
     * 图书位置（书架编号）
     */
    @Column(length = 50)
    private String location;

    /**
     * 图书简介
     */
    @Column(length = 2000)
    private String description;

    /**
     * 封面图片URL
     */
    @Column(length = 500)
    private String coverImage;

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
     * 图书分类枚举
     */
    public enum BookCategory {
        LITERATURE("文学"),
        SCIENCE("科学"),
        TECHNOLOGY("技术"),
        HISTORY("历史"),
        PHILOSOPHY("哲学"),
        ART("艺术"),
        ECONOMICS("经济"),
        MANAGEMENT("管理"),
        EDUCATION("教育"),
        MEDICINE("医学"),
        LAW("法律"),
        OTHER("其他");

        private final String description;

        BookCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 图书状态枚举
     */
    public enum BookStatus {
        AVAILABLE("可借"),
        BORROWED("已借出"),
        RESERVED("已预约"),
        LOST("遗失"),
        DAMAGED("损坏"),
        MAINTENANCE("维护中");

        private final String description;

        BookStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 检查图书是否可借
     */
    public boolean isAvailable() {
        return status == BookStatus.AVAILABLE && availableStock > 0;
    }

    /**
     * 检查图书是否可预约
     */
    public boolean isReservable() {
        return status != BookStatus.LOST && status != BookStatus.DAMAGED;
    }

    /**
     * 更新库存数量
     */
    public void updateStock(int borrowedChange) {
        this.borrowedCount += borrowedChange;
        this.availableStock = totalStock - borrowedCount;

        // 根据库存自动更新状态
        if (availableStock <= 0 && borrowedCount >= totalStock) {
            this.status = BookStatus.BORROWED;
        } else if (availableStock > 0) {
            this.status = BookStatus.AVAILABLE;
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
    }

    /**
     * JPA 生命周期回调：更新前设置时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
