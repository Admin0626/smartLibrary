package com.example.smart_library.controller;

import com.example.smart_library.common.Result;
import com.example.smart_library.dto.response.UserInfoResponse;
import com.example.smart_library.entity.User;
import com.example.smart_library.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户信息（根据ID）
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserInfoResponse> getUserById(@PathVariable Long userId) {
        UserInfoResponse response = userService.getUserById(userId);
        return Result.success(response);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<UserInfoResponse> getCurrentProfile(@RequestAttribute Long userId) {
        UserInfoResponse response = userService.getUserById(userId);
        return Result.success(response);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public Result<UserInfoResponse> updateProfile(@RequestAttribute Long userId,
                                                  @Valid @RequestBody User user) {
        UserInfoResponse response = userService.updateUser(userId, user);
        return Result.success("更新成功", response);
    }

    /**
     * 获取所有用户列表（分页）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<UserInfoResponse>> getAllUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<UserInfoResponse> response = userService.getAllUsers(pageable);
        return Result.success(response);
    }
}
