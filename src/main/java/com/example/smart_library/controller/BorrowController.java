package com.example.smart_library.controller;

import com.example.smart_library.common.Result;
import com.example.smart_library.dto.request.BorrowBookRequest;
import com.example.smart_library.dto.request.ReturnBookRequest;
import com.example.smart_library.dto.response.BorrowRecordResponse;
import com.example.smart_library.service.BorrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 借阅控制器
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    /**
     * 借书
     */
    @PostMapping
    public Result<BorrowRecordResponse> borrowBook(@RequestAttribute Long userId,
                                                   @Valid @RequestBody BorrowBookRequest request) {
        BorrowRecordResponse response = borrowService.borrowBook(userId, request);
        return Result.success("借书成功", response);
    }

    /**
     * 还书
     */
    @PostMapping("/return")
    public Result<BorrowRecordResponse> returnBook(@RequestAttribute Long userId,
                                                   @Valid @RequestBody ReturnBookRequest request) {
        BorrowRecordResponse response = borrowService.returnBook(userId, request);
        return Result.success("还书成功", response);
    }

    /**
     * 续借
     */
    @PostMapping("/{borrowRecordId}/renew")
    public Result<BorrowRecordResponse> renewBook(@RequestAttribute Long userId,
                                                  @PathVariable Long borrowRecordId) {
        BorrowRecordResponse response = borrowService.renewBook(userId, borrowRecordId);
        return Result.success("续借成功", response);
    }

    /**
     * 获取当前用户的借阅记录
     */
    @GetMapping("/my-records")
    public Result<Page<BorrowRecordResponse>> getMyBorrowRecords(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<BorrowRecordResponse> response = borrowService.getUserBorrowRecords(userId, pageNum, pageSize);
        return Result.success(response);
    }

    /**
     * 获取当前借阅中的记录
     */
    @GetMapping("/my-current")
    public Result<List<BorrowRecordResponse>> getCurrentBorrowRecords(@RequestAttribute Long userId) {
        List<BorrowRecordResponse> response = borrowService.getCurrentBorrowRecords(userId);
        return Result.success(response);
    }

    /**
     * 获取所有逾期的借阅记录（管理员）
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<BorrowRecordResponse>> getOverdueRecords() {
        List<BorrowRecordResponse> response = borrowService.getOverdueRecords();
        return Result.success(response);
    }
}
