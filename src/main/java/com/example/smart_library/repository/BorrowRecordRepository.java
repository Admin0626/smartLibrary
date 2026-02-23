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
 * 借阅记录Repository接口
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long>, JpaSpecificationExecutor<BorrowRecord> {

    /**
     * 根据用户ID查询借阅记录（分页）
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 借阅记录列表
     */
    Page<BorrowRecord> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据图书ID查询借阅记录（分页）
     *
     * @param bookId   图书ID
     * @param pageable 分页参数
     * @return 借阅记录列表
     */
    Page<BorrowRecord> findByBookId(Long bookId, Pageable pageable);

    /**
     * 根据用户ID和状态查询借阅记录
     *
     * @param userId 用户ID
     * @param status 借阅状态
     * @return 借阅记录列表
     */
    List<BorrowRecord> findByUserIdAndStatus(Long userId, BorrowRecord.BorrowStatus status);

    /**
     * 根据用户ID查询当前借阅中的记录数量
     *
     * @param userId 用户ID
     * @return 借阅数量
     */
    long countByUserIdAndStatus(Long userId, BorrowRecord.BorrowStatus status);

    /**
     * 查询逾期的借阅记录
     *
     * @return 逾期记录列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 'BORROWED' AND br.dueDate < :now")
    List<BorrowRecord> findOverdueRecords(@Param("now") LocalDateTime now);

    /**
     * 查询即将到期的借阅记录（3天内到期）
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 借阅记录列表
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.status = 'BORROWED' AND br.dueDate BETWEEN :startDate AND :endDate")
    List<BorrowRecord> findDueSoonRecords(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 检查用户是否已经借阅了某本书
     *
     * @param userId 用户ID
     * @param bookId 图书ID
     * @return 借阅记录
     */
    Optional<BorrowRecord> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, BorrowRecord.BorrowStatus status);

    /**
     * 统计用户的逾期次数
     *
     * @param userId 用户ID
     * @return 逾期次数
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.userId = :userId AND br.isOverdue = true")
    long countOverdueByUserId(@Param("userId") Long userId);

    /**
     * 查询用户在某本书上的借阅历史
     *
     * @param userId 用户ID
     * @param bookId 图书ID
     * @param pageable 分页参数
     * @return 借阅记录列表
     */
    Page<BorrowRecord> findByUserIdAndBookId(Long userId, Long bookId, Pageable pageable);
}
