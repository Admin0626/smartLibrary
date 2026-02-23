package com.example.smart_library.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图书搜索请求DTO
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchBookRequest {

    /**
     * 图书标题（模糊查询）
     */
    private String title;

    /**
     * 作者（模糊查询）
     */
    private String author;

    /**
     * ISBN（精确查询）
     */
    private String isbn;

    /**
     * 图书分类
     */
    private String category;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 图书状态
     */
    private String status;

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
