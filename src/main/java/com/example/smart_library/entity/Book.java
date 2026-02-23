package com.example.smart_library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
        @Index(name = "idx_title", columnList = "title"),
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_author", columnList = "author")
})
public class Book {

    /**
     * 图书ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ISBN编号（唯一）
     */
    @NotBlank(message = "ISBN不能为空")
    @Pattern(regexp = "^\\d{10,13}$", message = "ISBN格式不正确")
    @Column(unique = true, nullable = false, length = 20)
    private String isbn;

    /**
     * 图书标题
     */
    @NotBlank(message = "图书标题不能为空")
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
    @NotNull(message = "出版日期不能为空")
    @Column(nullable = false)
    private LocalDateTime publishDate;

    /**
     * 图书分类
     */
    @NotBlank(message = "图书分类不能为空")
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * 价格
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "价格必须大于0")
    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal price;

    /**
     * 总库存数量
     */
    @NotNull(message = "总库存不能为空")
    @Min(value = 0, message = "总库存不能为负数")
    @Column(nullable = false)
    private Integer totalStock = 0;

    /**
     * 当前可借数量
     */
    @NotNull(message = "可借数量不能为空")
    @Min(value = 0, message = "可借数量不能为负数")
    @Column(nullable = false)
    private Integer availableStock = 0;

    /**
     * 图书封面URL
     */
    @Column(length = 500)
    private String coverUrl;

    /**
     * 图书简介
     */
    @Column(length = 2000)
    private String description;

    /**
     * 图书位置（书架号）
     */
    @Column(length = 50)
    private String location;

    /**
     * 图书状态：AVAILABLE-可借, BORROWED-借出, RESERVED-预约中, MAINTENANCE-维护中
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookStatus status = BookStatus.AVAILABLE;

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
     * 图书状态枚举
     */
    public enum BookStatus {
        AVAILABLE("可借"),
        BORROWED("借出"),
        RESERVED("预约中"),
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
