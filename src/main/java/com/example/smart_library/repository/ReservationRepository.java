package com.example.smart_library.repository;

import com.example.smart_library.entity.Reservation;
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
 * 预约记录Repository接口
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    /**
     * 根据用户ID查询预约记录（分页）
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 预约记录列表
     */
    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据图书ID查询预约记录（按队列位置排序）
     *
     * @param bookId   图书ID
     * @param pageable 分页参数
     * @return 预约记录列表
     */
    Page<Reservation> findByBookIdOrderByQueuePositionAsc(Long bookId, Pageable pageable);

    /**
     * 根据用户ID和状态查询预约记录
     *
     * @param userId 用户ID
     * @param status 预约状态
     * @return 预约记录列表
     */
    List<Reservation> findByUserIdAndStatus(Long userId, Reservation.ReservationStatus status);

    /**
     * 检查用户是否已经预约了某本书（且预约未完成/取消/过期）
     *
     * @param userId 用户ID
     * @param bookId 图书ID
     * @return 预约记录
     */
    Optional<Reservation> findByUserIdAndBookIdAndStatusIn(Long userId, Long bookId, List<Reservation.ReservationStatus> statuses);

    /**
     * 查询某本书的有效预约数量
     *
     * @param bookId 图书ID
     * @return 预约数量
     */
    long countByBookIdAndStatus(Long bookId, Reservation.ReservationStatus status);

    /**
     * 查询过期的预约记录
     *
     * @param now 当前时间
     * @return 过期记录列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' AND r.expireTime < :now")
    List<Reservation> findExpiredReservations(@Param("now") LocalDateTime now);

    /**
     * 查询即将过期的预约记录（24小时内）
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 预约记录列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' AND r.expireTime BETWEEN :startTime AND :endTime")
    List<Reservation> findExpiringSoonReservations(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取某本书的预约队列（按队列位置排序）
     *
     * @param bookId 图书ID
     * @param status 预约状态
     * @return 预约记录列表
     */
    List<Reservation> findByBookIdAndStatusOrderByQueuePositionAsc(Long bookId, Reservation.ReservationStatus status);

    /**
     * 获取某本书下一个可以借阅的预约用户
     *
     * @param bookId 图书ID
     * @return 预约记录
     */
    @Query("SELECT r FROM Reservation r WHERE r.bookId = :bookId AND r.status = 'ACTIVE' ORDER BY r.queuePosition ASC")
    Optional<Reservation> findNextActiveReservation(@Param("bookId") Long bookId);

    /**
     * 统计用户的预约次数
     *
     * @param userId 用户ID
     * @return 预约次数
     */
    long countByUserId(Long userId);

    /**
     * 统计用户的违约次数（预约后未在规定时间内取书）
     *
     * @param userId 用户ID
     * @return 违约次数
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = :userId AND r.status = 'EXPIRED'")
    long countExpiredByUserId(@Param("userId") Long userId);
}
