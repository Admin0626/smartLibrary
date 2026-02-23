package com.example.smart_library.service;

import com.example.smart_library.dto.request.ReserveBookRequest;
import com.example.smart_library.dto.response.ReservationResponse;
import com.example.smart_library.entity.Book;
import com.example.smart_library.entity.Reservation;
import com.example.smart_library.entity.User;
import com.example.smart_library.repository.BookRepository;
import com.example.smart_library.repository.ReservationRepository;
import com.example.smart_library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约服务接口实现
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowService borrowService;
    private final BookService bookService;

    /**
     * 预约图书
     *
     * @param userId  用户ID
     * @param request 预约请求
     * @return 预约记录
     */
    @Transactional
    public ReservationResponse reserveBook(Long userId, ReserveBookRequest request) {
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查图书是否存在
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("图书不存在"));

        // 检查图书是否可借（如果可借则直接提示用户借阅）
        if (bookService.isAvailable(request.getBookId())) {
            throw new RuntimeException("该图书当前可借，请直接借阅，无需预约");
        }

        // 检查用户是否已经预约了这本书（未完成/取消/过期的预约）
        reservationRepository.findByUserIdAndBookIdAndStatusIn(
                userId,
                request.getBookId(),
                List.of(
                        Reservation.ReservationStatus.PENDING,
                        Reservation.ReservationStatus.ACTIVE
                )
        ).ifPresent(reservation -> {
            throw new RuntimeException("您已经预约了这本书，请勿重复预约");
        });

        // 计算队列位置
        long queuePosition = reservationRepository.countByBookIdAndStatus(
                request.getBookId(),
                Reservation.ReservationStatus.PENDING
        ) + 1;

        // 创建预约记录
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setBookId(request.getBookId());
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setExpireTime(LocalDateTime.now().plusDays(3));
        reservation.setQueuePosition((int) queuePosition);
        reservation.setNotificationStatus(Reservation.NotificationStatus.UNNOTIFIED);
        reservation.setStatus(Reservation.ReservationStatus.PENDING);
        reservation.setRemark(request.getRemark());

        reservation = reservationRepository.save(reservation);

        return convertToResponse(reservation, user, book);
    }

    /**
     * 取消预约
     *
     * @param userId         用户ID
     * @param reservationId  预约记录ID
     * @param cancelReason   取消原因
     * @return 预约记录
     */
    @Transactional
    public ReservationResponse cancelReservation(Long userId, Long reservationId, String cancelReason) {
        // 获取预约记录
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("预约记录不存在"));

        // 验证记录是否属于该用户
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此预约记录");
        }

        // 检查状态是否可以取消
        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING &&
                reservation.getStatus() != Reservation.ReservationStatus.ACTIVE) {
            throw new RuntimeException("该预约已完成或已取消，无法再次取消");
        }

        // 更新预约记录
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation.setCancelDate(LocalDateTime.now());
        reservation.setCancelReason(cancelReason);

        reservation = reservationRepository.save(reservation);

        // 重新排后续用户的队列位置
        updateQueuePositions(reservation.getBookId());

        // 获取用户和图书信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Book book = bookRepository.findById(reservation.getBookId())
                .orElseThrow(() -> new RuntimeException("图书不存在"));

        return convertToResponse(reservation, user, book);
    }

    /**
     * 获取用户的预约记录
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 预约记录列表
     */
    public Page<ReservationResponse> getUserReservations(Long userId, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Reservation> records = reservationRepository.findByUserId(userId, pageable);

        return records.map(record -> {
            User user = userRepository.findById(record.getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            Book book = bookRepository.findById(record.getBookId())
                    .orElseThrow(() -> new RuntimeException("图书不存在"));
            return convertToResponse(record, user, book);
        });
    }

    /**
     * 获取图书的预约队列
     *
     * @param bookId   图书ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 预约记录列表
     */
    public Page<ReservationResponse> getBookReservationQueue(Long bookId, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Reservation> records = reservationRepository.findByBookIdOrderByQueuePositionAsc(bookId, pageable);

        return records.map(record -> {
            User user = userRepository.findById(record.getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            Book book = bookRepository.findById(record.getBookId())
                    .orElseThrow(() -> new RuntimeException("图书不存在"));
            return convertToResponse(record, user, book);
        });
    }

    /**
     * 激活预约（当图书归还后，通知下一位预约用户）
     *
     * @param bookId 图书ID
     */
    @Transactional
    public void activateNextReservation(Long bookId) {
        // 检查图书是否可借
        if (!bookService.isAvailable(bookId)) {
            return;
        }

        // 获取下一个待激活的预约
        reservationRepository.findNextActiveReservation(bookId).ifPresent(reservation -> {
            reservation.setStatus(Reservation.ReservationStatus.ACTIVE);
            reservation.setNotificationStatus(Reservation.NotificationStatus.NOTIFIED);
            reservation.setNotifyTime(LocalDateTime.now());
            reservationRepository.save(reservation);
        });
    }

    /**
     * 处理过期的预约
     *
     * @return 处理的预约数量
     */
    @Transactional
    public int processExpiredReservations() {
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(LocalDateTime.now());

        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(Reservation.ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);

            // 重新排后续用户的队列位置
            updateQueuePositions(reservation.getBookId());

            // 激活下一个预约
            activateNextReservation(reservation.getBookId());
        }

        return expiredReservations.size();
    }

    /**
     * 更新队列位置
     *
     * @param bookId 图书ID
     */
    @Transactional
    private void updateQueuePositions(Long bookId) {
        List<Reservation> pendingReservations = reservationRepository.findByBookIdAndStatusOrderByQueuePositionAsc(
                bookId,
                Reservation.ReservationStatus.PENDING
        );

        for (int i = 0; i < pendingReservations.size(); i++) {
            pendingReservations.get(i).setQueuePosition(i + 1);
            reservationRepository.save(pendingReservations.get(i));
        }
    }

    /**
     * 将Reservation实体转换为ReservationResponse
     *
     * @param reservation 预约记录
     * @param user        用户
     * @param book        图书
     * @return 预约记录响应
     */
    private ReservationResponse convertToResponse(Reservation reservation, User user, Book book) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUserId())
                .username(user.getUsername())
                .bookId(reservation.getBookId())
                .bookTitle(book.getTitle())
                .bookIsbn(book.getIsbn())
                .reservationDate(reservation.getReservationDate())
                .expireTime(reservation.getExpireTime())
                .cancelDate(reservation.getCancelDate())
                .cancelReason(reservation.getCancelReason())
                .queuePosition(reservation.getQueuePosition())
                .notificationStatus(reservation.getNotificationStatus().name())
                .notificationStatusDescription(reservation.getNotificationStatus().getDescription())
                .notifyTime(reservation.getNotifyTime())
                .status(reservation.getStatus().name())
                .statusDescription(reservation.getStatus().getDescription())
                .remark(reservation.getRemark())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}
