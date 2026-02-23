package com.example.smart_library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 借阅记录响应DTO
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordResponse {

    /**
     * 借阅记录ID
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
     * 借阅日期
     */
    private LocalDateTime borrowDate;

    /**
     * 应还日期
     */
    private LocalDateTime dueDate;

    /**
     * 实际归还日期
     */
    private LocalDateTime returnDate;

    /**
     * 续借次数
     */
    private Integer renewCount;

    /**
     * 最大续借次数
     */
    private Integer maxRenewCount;

    /**
     * 是否逾期
     */
    private Boolean isOverdue;

    /**
     * 逾期天数
     */
    private Integer overdueDays;

    /**
     * 罚款金额
     */
    private BigDecimal fineAmount;

    /**
     * 借阅状态
     */
    private String status;

    /**
     * 借阅状态描述
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
