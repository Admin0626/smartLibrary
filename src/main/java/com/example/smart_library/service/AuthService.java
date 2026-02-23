package com.example.smart_library.service;

import com.example.smart_library.dto.request.LoginRequest;
import com.example.smart_library.dto.response.LoginResponse;
import com.example.smart_library.entity.User;
import com.example.smart_library.repository.UserRepository;
import com.example.smart_library.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务接口实现
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @param ip      登录IP
     * @return 登录响应
     */
    @Transactional
    public LoginResponse login(LoginRequest request, String ip) {
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == User.UserStatus.DISABLED) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }
        if (user.getStatus() == User.UserStatus.LOCKED) {
            throw new RuntimeException("账号已被锁定，请联系管理员");
        }

        // 使用JwtUtil生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        // 更新最后登录信息
        userService.updateLastLogin(user.getId(), ip);

        // 构建响应
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole().name())
                .roleDescription(user.getRole().getDescription())
                .status(user.getStatus().name())
                .build();
    }
}
