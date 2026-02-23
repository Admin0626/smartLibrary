package com.example.smart_library.repository;

import com.example.smart_library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 图书Repository接口
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    /**
     * 根据ISBN查找图书
     *
     * @param isbn ISBN编号
     * @return 图书信息
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * 检查ISBN是否存在
     *
     * @param isbn ISBN编号
     * @return 是否存在
     */
    boolean existsByIsbn(String isbn);

    /**
     * 根据标题模糊查询（分页）
     *
     * @param title    图书标题
     * @param pageable 分页参数
     * @return 图书列表
     */
    Page<Book> findByTitleContaining(String title, Pageable pageable);

    /**
     * 根据作者模糊查询（分页）
     *
     * @param author   作者
     * @param pageable 分页参数
     * @return 图书列表
     */
    Page<Book> findByAuthorContaining(String author, Pageable pageable);

    /**
     * 根据分类查询（分页）
     *
     * @param category 图书分类
     * @param pageable 分页参数
     * @return 图书列表
     */
    Page<Book> findByCategory(String category, Pageable pageable);

    /**
     * 根据状态查询（分页）
     *
     * @param status   图书状态
     * @param pageable 分页参数
     * @return 图书列表
     */
    Page<Book> findByStatus(Book.BookStatus status, Pageable pageable);

    /**
     * 多条件综合搜索
     *
     * @param title    图书标题（可选）
     * @param author   作者（可选）
     * @param isbn     ISBN（可选）
     * @param category 分类（可选）
     * @param publisher 出版社（可选）
     * @param status   状态（可选）
     * @param pageable 分页参数
     * @return 图书列表
     */
    @Query("SELECT b FROM Book b WHERE " +
            "(:title IS NULL OR b.title LIKE %:title%) AND " +
            "(:author IS NULL OR b.author LIKE %:author%) AND " +
            "(:isbn IS NULL OR b.isbn = :isbn) AND " +
            "(:category IS NULL OR b.category = :category) AND " +
            "(:publisher IS NULL OR b.publisher LIKE %:publisher%) AND " +
            "(:status IS NULL OR b.status = :status)")
    Page<Book> searchBooks(
            @Param("title") String title,
            @Param("author") String author,
            @Param("isbn") String isbn,
            @Param("category") String category,
            @Param("publisher") String publisher,
            @Param("status") Book.BookStatus status,
            Pageable pageable);

    /**
     * 查询可借阅的图书（分页）
     *
     * @param pageable 分页参数
     * @return 图书列表
     */
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' AND b.availableStock > 0")
    Page<Book> findAvailableBooks(Pageable pageable);

    /**
     * 查询热门图书（按借阅次数排序，这里简化为按可借数量倒序）
     *
     * @param limit 限制数量
     * @return 图书列表
     */
    @Query("SELECT b FROM Book b WHERE b.availableStock > 0 ORDER BY b.totalStock DESC")
    List<Book> findPopularBooks(Pageable pageable);
}
