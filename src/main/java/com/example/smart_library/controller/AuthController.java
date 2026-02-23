package com.example.smart_library.controller;

import com.example.smart_library.common.Result;
import com.example.smart_library.dto.request.LoginRequest;
import com.example.smart_library.dto.request.RegisterRequest;
import com.example.smart_library.dto.response.LoginResponse;
import com.example.smart_library.dto.response.UserInfoResponse;
import com.example.smart_library.service.AuthService;
import com.example.smart_library.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        LoginResponse response = authService.login(request, ip);
        return Result.success("登录成功", response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserInfoResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserInfoResponse response = userService.register(request);
        return Result.success("注册成功", response);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public Result<UserInfoResponse> getCurrentUser(HttpServletRequest request) {
        // 从请求属性中获取用户ID（由JWT过滤器设置）
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        UserInfoResponse response = userService.getUserById(userId);
        return Result.success(response);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
