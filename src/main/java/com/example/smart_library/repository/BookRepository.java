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

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    /**
     * 根据 ISBN 查询图书
     *
     * @param isbn ISBN
     * @return 图书信息
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * 检查 ISBN 是否已存在
     *
     * @param isbn ISBN
     * @return 是否存在
     */
    boolean existsByIsbn(String isbn);

    /**
     * 根据书名模糊查询图书
     *
     * @param title 书名关键词
     * @return 图书列表
     */
    List<Book> findByTitleContaining(String title);

    /**
     * 根据作者查询图书
     *
     * @param author 作者
     * @return 图书列表
     */
    List<Book> findByAuthor(String author);

    /**
     * 根据分类查询图书列表
     *
     * @param category 图书分类
     * @return 图书列表
     */
    List<Book> findByCategory(Book.BookCategory category);

    /**
     * 根据分类查询图书列表（分页）
     *
     * @param category 图书分类
     * @param pageable 分页参数
     * @return 图书分页列表
     */
    Page<Book> findByCategory(Book.BookCategory category, Pageable pageable);

    /**
     * 根据状态查询图书列表
     *
     * @param status 图书状态
     * @return 图书列表
     */
    List<Book> findByStatus(Book.BookStatus status);

    /**
     * 根据状态查询图书列表（分页）
     *
     * @param status 图书状态
     * @param pageable 分页参数
     * @return 图书分页列表
     */
    Page<Book> findByStatus(Book.BookStatus status, Pageable pageable);

    /**
     * 查询所有可借的图书（状态为 AVAILABLE 且可借数量 > 0）
     *
     * @return 图书列表
     */
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' AND b.availableStock > 0")
    List<Book> findAvailableBooks();

    /**
     * 查询所有可借的图书（分页）
     *
     * @param pageable 分页参数
     * @return 图书分页列表
     */
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' AND b.availableStock > 0")
    Page<Book> findAvailableBooks(Pageable pageable);

    /**
     * 根据书名、作者、ISBN 模糊查询图书
     *
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 图书分页列表
     */
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.isbn LIKE %:keyword%")
    Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据分类和状态查询图书（分页）
     *
     * @param category 图书分类
     * @param status 图书状态
     * @param pageable 分页参数
     * @return 图书分页列表
     */
    Page<Book> findByCategoryAndStatus(Book.BookCategory category, Book.BookStatus status, Pageable pageable);

    /**
     * 根据出版社查询图书
     *
     * @param publisher 出版社
     * @return 图书列表
     */
    List<Book> findByPublisher(String publisher);

    /**
     * 根据出版社查询图书（分页）
     *
     * @param publisher 出版社
     * @param pageable 分页参数
     * @return 图书分页列表
     */
    Page<Book> findByPublisher(String publisher, Pageable pageable);

    /**
     * 查询库存不足的图书（可借数量 < 5）
     *
     * @return 图书列表
     */
    @Query("SELECT b FROM Book b WHERE b.availableStock < 5 AND b.status != 'LOST' AND b.status != 'DAMAGED'")
    List<Book> findLowStockBooks();

    /**
     * 统计图书总数
     *
     * @param category 图书分类（可选）
     * @return 图书总数
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE (:category IS NULL OR b.category = :category)")
    long countByCategory(@Param("category") Book.BookCategory category);

    /**
     * 统计可借图书总数
     *
     * @return 可借图书总数
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.status = 'AVAILABLE' AND b.availableStock > 0")
    long countAvailableBooks();

    /**
     * 更新图书库存
     *
     * @param bookId 图书ID
     * @param borrowedChange 借出数量变化
     */
    @Query("UPDATE Book b SET b.borrowedCount = b.borrowedCount + :borrowedChange, b.availableStock = b.totalStock - b.borrowedCount WHERE b.id = :bookId")
    void updateBookStock(@Param("bookId") Long bookId, @Param("borrowedChange") Integer borrowedChange);

    /**
     * 更新图书状态
     *
     * @param bookId 图书ID
     * @param status 新状态
     */
    @Query("UPDATE Book b SET b.status = :status WHERE b.id = :bookId")
    void updateBookStatus(@Param("bookId") Long bookId, @Param("status") Book.BookStatus status);

    /**
     * 根据价格范围查询图书
     *
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param pageable 分页参数
     * @return 图书分页列表
     */
    @Query("SELECT b FROM Book b WHERE b.price BETWEEN :minPrice AND :maxPrice")
    Page<Book> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice,
                                @Param("maxPrice") java.math.BigDecimal maxPrice,
                                Pageable pageable);

    /**
     * 查询借出次数最多的图书（热门图书）
     *
     * @param pageable 分页参数
     * @return 图书分页列表
     */
    @Query("SELECT b FROM Book b ORDER BY b.borrowedCount DESC")
    Page<Book> findPopularBooks(Pageable pageable);

    /**
     * 根据位置查询图书
     *
     * @param location 位置
     * @return 图书列表
     */
    List<Book> findByLocation(String location);

    /**
     * 查询遗失或损坏的图书
     *
     * @return 图书列表
     */
    @Query("SELECT b FROM Book b WHERE b.status = 'LOST' OR b.status = 'DAMAGED'")
    List<Book> findLostOrDamagedBooks();
}