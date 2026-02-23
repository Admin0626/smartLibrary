package com.example.smart_library.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应包装类
 * 解决 Spring Boot 3.x 不支持直接序列化 PageImpl 的问题
 *
 * @param <T> 数据类型
 * @author SmartLibrary
 * @since 2024-02-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * 当前页数据列表
     */
    private List<T> content;

    /**
     * 当前页码
     */
    private Integer currentPage;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 总记录数
     */
    private Long totalElements;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否第一页
     */
    private Boolean isFirst;

    /**
     * 是否最后一页
     */
    private Boolean isLast;

    /**
     * 从 Spring Data Page 对象转换
     *
     * @param page Spring Data Page 对象
     * @param <T>  数据类型
     * @return PageResponse 对象
     */
    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(page.getContent());
        response.setCurrentPage(page.getNumber() + 1);
        response.setPageSize(page.getSize());
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        response.setHasPrevious(page.hasPrevious());
        response.setHasNext(page.hasNext());
        response.setIsFirst(page.isFirst());
        response.setIsLast(page.isLast());
        return response;
    }
}
