package com.example.smart_library.service;

import com.example.smart_library.dto.request.BorrowBookRequest;
import com.example.smart_library.dto.request.ReturnBookRequest;
import com.example.smart_library.dto.response.BorrowRecordResponse;
import com.example.smart_library.entity.Book;
import com.example.smart_library.entity.BorrowRecord;
import com.example.smart_library.entity.User;
import com.example.smart_library.repository.BorrowRecordRepository;
import com.example.smart_library.repository.BookRepository;
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
 * 借阅服务接口实现
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final BookService bookService;

    /**
     * 借书
     *
     * @param userId  用户ID
     * @param request 借书请求
     * @return 借阅记录
     */
    @Transactional
    public BorrowRecordResponse borrowBook(Long userId, BorrowBookRequest request) {
        // 检查用户是否可以借书
        if (!userService.canBorrow(userId)) {
            throw new RuntimeException("用户状态异常或已达到最大借阅数量");
        }

        // 检查图书是否可借
        if (!bookService.isAvailable(request.getBookId())) {
            throw new RuntimeException("该图书暂无可借数量");
        }

        // 检查用户是否已经借阅了这本书
        borrowRecordRepository.findByUserIdAndBookIdAndStatus(userId, request.getBookId(), BorrowRecord.BorrowStatus.BORROWED)
                .ifPresent(record -> {
                    throw new RuntimeException("您已经借阅了这本书，请先归还后再借阅");
                });

        // 获取用户和图书信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("图书不存在"));

        // 创建借阅记录
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setUserId(userId);
        borrowRecord.setBookId(request.getBookId());
        borrowRecord.setBorrowDate(LocalDateTime.now());
        borrowRecord.setDueDate(LocalDateTime.now().plusDays(request.getBorrowDays()));
        borrowRecord.setStatus(BorrowRecord.BorrowStatus.BORROWED);
        borrowRecord.setRenewCount(0);

        // 保存借阅记录
        borrowRecord = borrowRecordRepository.save(borrowRecord);

        // 更新用户借阅数量
        userService.increaseBorrowCount(userId);

        // 更新图书可借数量
        bookService.decreaseAvailableStock(request.getBookId());

        return convertToResponse(borrowRecord, user, book);
    }

    /**
     * 还书
     *
     * @param userId  用户ID
     * @param request 还书请求
     * @return 借阅记录
     */
    @Transactional
    public BorrowRecordResponse returnBook(Long userId, ReturnBookRequest request) {
        // 获取借阅记录
        BorrowRecord borrowRecord = borrowRecordRepository.findById(request.getBorrowRecordId())
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));

        // 验证记录是否属于该用户
        if (!borrowRecord.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此借阅记录");
        }

        // 检查是否已经归还
        if (borrowRecord.getStatus() == BorrowRecord.BorrowStatus.RETURNED) {
            throw new RuntimeException("该图书已归还");
        }

        // 获取用户和图书信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Book book = bookRepository.findById(borrowRecord.getBookId())
                .orElseThrow(() -> new RuntimeException("图书不存在"));

        // 更新借阅记录
        borrowRecord.setReturnDate(LocalDateTime.now());
        borrowRecord.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        borrowRecord.setRemark(request.getRemark());

        // 触发 @PreUpdate 回调，计算逾期罚款
        borrowRecord = borrowRecordRepository.save(borrowRecord);

        // 更新用户借阅数量
        userService.decreaseBorrowCount(userId);

        // 更新图书可借数量
        bookService.increaseAvailableStock(borrowRecord.getBookId());

        return convertToResponse(borrowRecord, user, book);
    }

    /**
     * 续借
     *
     * @param userId        用户ID
     * @param borrowRecordId 借阅记录ID
     * @return 借阅记录
     */
    @Transactional
    public BorrowRecordResponse renewBook(Long userId, Long borrowRecordId) {
        // 获取借阅记录
        BorrowRecord borrowRecord = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));

        // 验证记录是否属于该用户
        if (!borrowRecord.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此借阅记录");
        }

        // 检查是否可以续借
        if (borrowRecord.getStatus() != BorrowRecord.BorrowStatus.BORROWED) {
            throw new RuntimeException("该图书已归还或无法续借");
        }

        if (borrowRecord.getRenewCount() >= borrowRecord.getMaxRenewCount()) {
            throw new RuntimeException("已达到最大续借次数");
        }

        // 检查是否逾期（逾期不允许续借）
        if (borrowRecord.getIsOverdue()) {
            throw new RuntimeException("已逾期，无法续借，请先归还并缴纳罚款");
        }

        // 更新续借信息
        borrowRecord.setRenewCount(borrowRecord.getRenewCount() + 1);
        borrowRecord.setDueDate(borrowRecord.getDueDate().plusDays(30)); // 续借增加30天

        borrowRecord = borrowRecordRepository.save(borrowRecord);

        // 获取用户和图书信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Book book = bookRepository.findById(borrowRecord.getBookId())
                .orElseThrow(() -> new RuntimeException("图书不存在"));

        return convertToResponse(borrowRecord, user, book);
    }

    /**
     * 获取用户的借阅记录
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 借阅记录列表
     */
    public Page<BorrowRecordResponse> getUserBorrowRecords(Long userId, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<BorrowRecord> records = borrowRecordRepository.findByUserId(userId, pageable);

        return records.map(record -> {
            User user = userRepository.findById(record.getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            Book book = bookRepository.findById(record.getBookId())
                    .orElseThrow(() -> new RuntimeException("图书不存在"));
            return convertToResponse(record, user, book);
        });
    }

    /**
     * 获取当前借阅中的记录
     *
     * @param userId 用户ID
     * @return 借阅记录列表
     */
    public List<BorrowRecordResponse> getCurrentBorrowRecords(Long userId) {
        List<BorrowRecord> records = borrowRecordRepository.findByUserIdAndStatus(userId, BorrowRecord.BorrowStatus.BORROWED);

        return records.stream().map(record -> {
            User user = userRepository.findById(record.getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            Book book = bookRepository.findById(record.getBookId())
                    .orElseThrow(() -> new RuntimeException("图书不存在"));
            return convertToResponse(record, user, book);
        }).toList();
    }

    /**
     * 获取逾期的借阅记录
     *
     * @return 逾期记录列表
     */
    public List<BorrowRecordResponse> getOverdueRecords() {
        List<BorrowRecord> records = borrowRecordRepository.findOverdueRecords(LocalDateTime.now());

        return records.stream().map(record -> {
            User user = userRepository.findById(record.getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            Book book = bookRepository.findById(record.getBookId())
                    .orElseThrow(() -> new RuntimeException("图书不存在"));
            return convertToResponse(record, user, book);
        }).toList();
    }

    /**
     * 将BorrowRecord实体转换为BorrowRecordResponse
     *
     * @param record 借阅记录
     * @param user   用户
     * @param book   图书
     * @return 借阅记录响应
     */
    private BorrowRecordResponse convertToResponse(BorrowRecord record, User user, Book book) {
        return BorrowRecordResponse.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .username(user.getUsername())
                .bookId(record.getBookId())
                .bookTitle(book.getTitle())
                .bookIsbn(book.getIsbn())
                .borrowDate(record.getBorrowDate())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .renewCount(record.getRenewCount())
                .maxRenewCount(record.getMaxRenewCount())
                .isOverdue(record.getIsOverdue())
                .overdueDays(record.getOverdueDays())
                .fineAmount(record.getFineAmount())
                .status(record.getStatus().name())
                .statusDescription(record.getStatus().getDescription())
                .remark(record.getRemark())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
