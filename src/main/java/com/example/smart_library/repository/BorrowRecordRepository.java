package com.example.smart_library.repository;

import com.example.smart_library.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 借阅记录 Repository 接口
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long>, JpaSpecificationExecutor<BorrowRecord> {

    /**
     * 根据用户ID查询借阅记录列表
     *
     * @param userId 用户ID
     * @return 借阅记录列表
     */
    List<BorrowRecord> findByUserId(Long userId);

    /**
     * 根据用户ID查询借阅记录列表（分页）
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    Page<BorrowRecord> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据图书ID查询借阅记录列表
     *
     * @param bookId 图书ID
     * @return 借阅记录列表
     */
    List<BorrowRecord> findByBookId(Long bookId);

    /**
     * 根据图书ID查询借阅记录列表（分页）
     *
     * @param bookId 图书ID
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    Page<BorrowRecord> findByBookId(Long bookId, Pageable pageable);

    /**
     * 根据状态查询借阅记录列表
     *
     * @param status 借阅状态
     * @return 借阅记录列表
     */
    List<BorrowRecord> findByStatus(BorrowRecord.BorrowStatus status);

    /**
     * 根据状态查询借阅记录列表（分页）
     *
     * @param status 借阅状态
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    Page<BorrowRecord> findByStatus(BorrowRecord.BorrowStatus status, Pageable pageable);

    /**
     * 查询用户当前借阅中的记录
     *
     * @param userId 用户ID
     * @return 借阅记录列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.userId = :userId AND br.status = 'BORROWING'")
    List<BorrowRecord> findActiveBorrowsByUserId(@Param("userId") Long userId);

    /**
     * 统计用户当前借阅数量
     *
     * @param userId 用户ID
     * @return 借阅数量
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.userId = :userId AND br.status = 'BORROWING'")
    long countActiveBorrowsByUserId(@Param("userId") Long userId);

    /**
     * 查询已逾期的借阅记录
     *
     * @return 借阅记录列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 'BORROWING' AND br.dueDate < :currentTime")
    List<BorrowRecord> findOverdueBorrows(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询已逾期的借阅记录（使用当前时间）
     *
     * @return 借阅记录列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 'BORROWING' AND br.dueDate < CURRENT_TIMESTAMP")
    List<BorrowRecord> findOverdueBorrows();

    /**
     * 查询用户已逾期的借阅记录
     *
     * @param userId 用户ID
     * @return 借阅记录列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.userId = :userId AND br.status = 'BORROWING' AND br.dueDate < CURRENT_TIMESTAMP")
    List<BorrowRecord> findOverdueBorrowsByUserId(@Param("userId") Long userId);

    /**
     * 查询即将到期的借阅记录（3天内到期）
     *
     * @return 借阅记录列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 'BORROWING' AND br.dueDate BETWEEN CURRENT_TIMESTAMP AND :dueTime")
    List<BorrowRecord> findDueSoonBorrows(@Param("dueTime") LocalDateTime dueTime);

    /**
     * 根据用户名或图书标题模糊查询借阅记录
     *
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.userName LIKE %:keyword% OR br.bookTitle LIKE %:keyword%")
    Page<BorrowRecord> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查询借阅历史（指定时间段内）
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 借阅记录列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.borrowDate BETWEEN :startDate AND :endDate")
    List<BorrowRecord> findBorrowsByDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * 查询用户借阅历史（指定时间段内）
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 借阅记录列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.userId = :userId AND br.borrowDate BETWEEN :startDate AND :endDate")
    List<BorrowRecord> findBorrowsByUserIdAndDateRange(@Param("userId") Long userId,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    /**
     * 统计借阅记录总数
     *
     * @param status 借阅状态（可选）
     * @return 借阅记录总数
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE (:status IS NULL OR br.status = :status)")
    long countByStatus(@Param("status") BorrowRecord.BorrowStatus status);

    /**
     * 统计用户借阅记录总数
     *
     * @param userId 用户ID
     * @return 借阅记录总数
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 更新借阅记录状态
     *
     * @param recordId 借阅记录ID
     * @param status 新状态
     */
    @Query("UPDATE BorrowRecord br SET br.status = :status WHERE br.id = :recordId")
    void updateBorrowStatus(@Param("recordId") Long recordId, @Param("status") BorrowRecord.BorrowStatus status);

    /**
     * 更新借阅记录归还信息
     *
     * @param recordId 借阅记录ID
     * @param returnDate 归还日期
     * @param status 新状态
     */
    @Query("UPDATE BorrowRecord br SET br.returnDate = :returnDate, br.status = :status, br.overdueDays = br.overdueDays + :additionalDays, br.fine = br.fine + :additionalFine WHERE br.id = :recordId")
    void updateReturnInfo(@Param("recordId") Long recordId,
                          @Param("returnDate") LocalDateTime returnDate,
                          @Param("status") BorrowRecord.BorrowStatus status,
                          @Param("additionalDays") Integer additionalDays,
                          @Param("additionalFine") java.math.BigDecimal additionalFine);

    /**
     * 查询逾期未还的借阅记录（包括已逾期状态）
     *
     * @return 借阅记录列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE (br.status = 'BORROWING' AND br.dueDate < CURRENT_TIMESTAMP) OR br.status = 'OVERDUE'")
    List<BorrowRecord> findAllOverdueBorrows();

    /**
     * 查询用户当前借阅的指定图书
     *
     * @param userId 用户ID
     * @param bookId 图书ID
     * @return 借阅记录
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.userId = :userId AND br.bookId = :bookId AND br.status = 'BORROWING'")
    Optional<BorrowRecord> findActiveBorrowByUserAndBook(@Param("userId") Long userId,
                                                         @Param("bookId") Long bookId);

    /**
     * 查询累计罚金最多的借阅记录
     *
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.fine > 0 ORDER BY br.fine DESC")
    Page<BorrowRecord> findBorrowsWithHighestFines(Pageable pageable);

    /**
     * 查询未支付罚金的借阅记录
     *
     * @return 借阅记录列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.fine > 0 AND br.finePaid = false")
    List<BorrowRecord> findBorrowsWithUnpaidFines();

    /**
     * 查询用户未支付罚金的借阅记录
     *
     * @param userId 用户ID
     * @return 借阅记录列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.userId = :userId AND br.fine > 0 AND br.finePaid = false")
    List<BorrowRecord> findBorrowsWithUnpaidFinesByUserId(@Param("userId") Long userId);

    /**
     * 查询遗失的借阅记录
     *
     * @return 借阅记录列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 'LOST'")
    List<BorrowRecord> findLostBorrows();

    /**
     * 查询热门图书（根据借阅次数）
     *
     * @param pageable 分页参数
     * @return 借阅记录分页列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.status IN ('RETURNED', 'OVERDUE') GROUP BY br.bookId ORDER BY COUNT(br.bookId) DESC")
    Page<BorrowRecord> findPopularBooks(Pageable pageable);
}
