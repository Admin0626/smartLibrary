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
 * 预约记录 Repository 接口
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    /**
     * 根据用户ID查询预约记录列表
     *
     * @param userId 用户ID
     * @return 预约记录列表
     */
    List<Reservation> findByUserId(Long userId);

    /**
     * 根据用户ID查询预约记录列表（分页）
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 预约记录分页列表
     */
    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据图书ID查询预约记录列表
     *
     * @param bookId 图书ID
     * @return 预约记录列表
     */
    List<Reservation> findByBookId(Long bookId);

    /**
     * 根据图书ID查询预约记录列表（分页）
     *
     * @param bookId 图书ID
     * @param pageable 分页参数
     * @return 预约记录分页列表
     */
    Page<Reservation> findByBookId(Long bookId, Pageable pageable);

    /**
     * 根据状态查询预约记录列表
     *
     * @param status 预约状态
     * @return 预约记录列表
     */
    List<Reservation> findByStatus(Reservation.ReservationStatus status);

    /**
     * 根据状态查询预约记录列表（分页）
     *
     * @param status 预约状态
     * @param pageable 分页参数
     * @return 预约记录分页列表
     */
    Page<Reservation> findByStatus(Reservation.ReservationStatus status, Pageable pageable);

    /**
     * 查询用户当前待处理的预约记录
     *
     * @param userId 用户ID
     * @return 预约记录列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND r.status = 'PENDING'")
    List<Reservation> findPendingReservationsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户所有待处理的预约记录（包括 PENDING 和 CONFIRMED）
     *
     * @param userId 用户ID
     * @return 预约记录列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND r.status IN ('PENDING', 'CONFIRMED')")
    List<Reservation> findActiveReservationsByUserId(@Param("userId") Long userId);

    /**
     * 查询图书的排队预约列表（按预约时间排序）
     *
     * @param bookId 图书ID
     * @return 预约记录列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.bookId = :bookId AND r.status = 'PENDING' AND r.reservationType = 'WAITING_LIST' ORDER BY r.reservationTime ASC")
    List<Reservation> findWaitingListByBookId(@Param("bookId") Long bookId);

    /**
     * 查询图书的预约数量（待处理状态）
     *
     * @param bookId 图书ID
     * @return 预约数量
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.bookId = :bookId AND r.status = 'PENDING'")
    long countPendingReservationsByBookId(@Param("bookId") Long bookId);

    /**
     * 查询用户的预约数量（待处理状态）
     *
     * @param userId 用户ID
     * @return 预约数量
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = :userId AND r.status = 'PENDING'")
    long countPendingReservationsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户是否已预约指定图书（待处理或已确认状态）
     *
     * @param userId 用户ID
     * @param bookId 图书ID
     * @return 预约记录
     */
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND r.bookId = :bookId AND r.status IN ('PENDING', 'CONFIRMED')")
    Optional<Reservation> findActiveReservationByUserAndBook(@Param("userId") Long userId,
                                                             @Param("bookId") Long bookId);

    /**
     * 查询已过期的预约记录
     *
     * @return 预约记录列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' " +
            "AND r.reservationTime < CURRENT_TIMESTAMP - r.validDays DAY")
    List<Reservation> findExpiredReservations();

    /**
     * 查询即将过期的预约记录（1天内过期）
     *
     * @return 预约记录列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' " +
            "AND r.reservationTime + r.validDays DAY BETWEEN CURRENT_TIMESTAMP AND :expiryTime")
    List<Reservation> findExpiringSoonReservations(@Param("expiryTime") LocalDateTime expiryTime);

    /**
     * 根据预约类型查询预约记录列表
     *
     * @param reservationType 预约类型
     * @return 预约记录列表
     */
    List<Reservation> findByReservationType(Reservation.ReservationType reservationType);

    /**
     * 根据预约类型查询预约记录列表（分页）
     *
     * @param reservationType 预约类型
     * @param pageable 分页参数
     * @return 预约记录分页列表
     */
    Page<Reservation> findByReservationType(Reservation.ReservationType reservationType, Pageable pageable);

    /**
     * 根据用户名或图书标题模糊查询预约记录
     *
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 预约记录分页列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.userName LIKE %:keyword% OR r.bookTitle LIKE %:keyword%")
    Page<Reservation> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查询预约历史（指定时间段内）
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 预约记录列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.reservationTime BETWEEN :startDate AND :endDate")
    List<Reservation> findReservationsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    /**
     * 查询用户预约历史（指定时间段内）
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 预约记录列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND r.reservationTime BETWEEN :startDate AND :endDate")
    List<Reservation> findReservationsByUserIdAndDateRange(@Param("userId") Long userId,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    /**
     * 统计预约记录总数
     *
     * @param status 预约状态（可选）
     * @return 预约记录总数
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE (:status IS NULL OR r.status = :status)")
    long countByStatus(@Param("status") Reservation.ReservationStatus status);

    /**
     * 统计用户预约记录总数
     *
     * @param userId 用户ID
     * @return 预约记录总数
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 更新预约记录状态
     *
     * @param reservationId 预约记录ID
     * @param status 新状态
     */
    @Query("UPDATE Reservation r SET r.status = :status WHERE r.id = :reservationId")
    void updateReservationStatus(@Param("reservationId") Long reservationId, @Param("status") Reservation.ReservationStatus status);

    /**
     * 更新预约序号（排队位置）
     *
     * @param bookId 图书ID
     */
    @Query("UPDATE Reservation r SET r.queueNumber = (SELECT COUNT(*) FROM Reservation r2 WHERE r2.bookId = :bookId AND r2.status = 'PENDING' AND r2.reservationTime <= r.reservationTime) WHERE r.bookId = :bookId AND r.status = 'PENDING'")
    void updateQueueNumbers(@Param("bookId") Long bookId);

    /**
     * 查询预约序号最小的记录（排在第一位）
     *
     * @param bookId 图书ID
     * @return 预约记录
     */
    @Query("SELECT r FROM Reservation r WHERE r.bookId = :bookId AND r.status = 'PENDING' ORDER BY r.queueNumber ASC")
    Optional<Reservation> findFirstInQueue(@Param("bookId") Long bookId);

    /**
     * 查询已确认但未完成的预约记录
     *
     * @return 预约记录列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = 'CONFIRMED' AND r.pickupTime < CURRENT_TIMESTAMP")
    List<Reservation> findConfirmedReservationsExpired();

    /**
     * 查询热门图书（根据预约次数）
     *
     * @param pageable 分页参数
     * @return 预约记录分页列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.status IN ('COMPLETED', 'CANCELLED') GROUP BY r.bookId ORDER BY COUNT(r.bookId) DESC")
    Page<Reservation> findPopularBooks(Pageable pageable);

    /**
     * 查询指定日期范围内的预约记录（按预约时间排序）
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageable 分页参数
     * @return 预约记录分页列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.reservationTime BETWEEN :startDate AND :endDate ORDER BY r.reservationTime DESC")
    Page<Reservation> findReservationsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate,
                                                  Pageable pageable);

    /**
     * 查询未通知的预约记录
     *
     * @return 预约记录列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' AND r.notified = false")
    List<Reservation> findUnnotifiedReservations();

    /**
     * 查询用户已完成的预约记录
     *
     * @param userId 用户ID
     * @return 预约记录列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND r.status = 'COMPLETED'")
    List<Reservation> findCompletedReservationsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户已取消的预约记录
     *
     * @param userId 用户ID
     * @return 预约记录列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.userId = :userId AND r.status = 'CANCELLED'")
    List<Reservation> findCancelledReservationsByUserId(@Param("userId") Long userId);
}
