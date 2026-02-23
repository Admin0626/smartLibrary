package com.example.smart_library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 图书信息响应DTO
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookInfoResponse {

    /**
     * 图书ID
     */
    private Long id;

    /**
     * ISBN编号
     */
    private String isbn;

    /**
     * 图书标题
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 出版日期
     */
    private LocalDateTime publishDate;

    /**
     * 图书分类
     */
    private String category;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 总库存数量
     */
    private Integer totalStock;

    /**
     * 当前可借数量
     */
    private Integer availableStock;

    /**
     * 图书封面URL
     */
    private String coverUrl;

    /**
     * 图书简介
     */
    private String description;

    /**
     * 图书位置
     */
    private String location;

    /**
     * 图书状态
     */
    private String status;

    /**
     * 图书状态描述
     */
    private String statusDescription;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
