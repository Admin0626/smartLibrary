package com.example.smart_library.controller;

import com.example.smart_library.common.PageResponse;
import com.example.smart_library.common.Result;
import com.example.smart_library.dto.request.ReserveBookRequest;
import com.example.smart_library.dto.response.ReservationResponse;
import com.example.smart_library.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 预约控制器
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 预约图书
     */
    @PostMapping
    public Result<ReservationResponse> reserveBook(@RequestAttribute Long userId,
                                                   @Valid @RequestBody ReserveBookRequest request) {
        ReservationResponse response = reservationService.reserveBook(userId, request);
        return Result.success("预约成功", response);
    }

    /**
     * 取消预约
     */
    @PostMapping("/{reservationId}/cancel")
    public Result<ReservationResponse> cancelReservation(
            @RequestAttribute Long userId,
            @PathVariable Long reservationId,
            @RequestBody(required = false) Map<String, String> requestBody) {
        String reason = requestBody != null ? requestBody.get("reason") : null;
        ReservationResponse response = reservationService.cancelReservation(userId, reservationId, reason);
        return Result.success("取消成功", response);
    }

    /**
     * 获取当前用户的预约记录
     */
    @GetMapping("/my-reservations")
    public Result<PageResponse<ReservationResponse>> getMyReservations(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        org.springframework.data.domain.Page<ReservationResponse> page = reservationService.getUserReservations(userId, pageNum, pageSize);
        PageResponse<ReservationResponse> response = PageResponse.of(page);
        return Result.success(response);
    }

    /**
     * 获取图书的预约队列
     */
    @GetMapping("/book/{bookId}/queue")
    public Result<PageResponse<ReservationResponse>> getBookReservationQueue(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        org.springframework.data.domain.Page<ReservationResponse> page = reservationService.getBookReservationQueue(bookId, pageNum, pageSize);
        PageResponse<ReservationResponse> response = PageResponse.of(page);
        return Result.success(response);
    }
}
