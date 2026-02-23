package com.example.smart_library.service;

import com.example.smart_library.dto.request.LoginRequest;
import com.example.smart_library.dto.response.LoginResponse;
import com.example.smart_library.entity.User;
import com.example.smart_library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    /**
     * JWT 密钥（实际项目中应配置在配置文件中）
     */
    private static final String JWT_SECRET = "SmartLibrarySecretKey2024ForJWTTokenGeneration";
    /**
     * JWT 过期时间（毫秒）：7天
     */
    private static final long JWT_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

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

        // 生成JWT令牌
        String token = generateToken(user);

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

    /**
     * 生成JWT令牌
     *
     * @param user 用户信息
     * @return JWT令牌
     */
    private String generateToken(User user) {
        // 这里使用简化的JWT生成逻辑
        // 实际项目中应使用 io.jsonwebtoken:jjwt 等专业库
        long now = System.currentTimeMillis();
        String payload = String.format("{\"userId\":%d,\"username\":\"%s\",\"role\":\"%s\",\"exp\":%d}",
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                now + JWT_EXPIRATION);

        // 简单的Base64编码（实际应使用更安全的签名算法）
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String encodedHeader = java.util.Base64.getUrlEncoder().encodeToString(header.getBytes());
        String encodedPayload = java.util.Base64.getUrlEncoder().encodeToString(payload.getBytes());
        String signature = java.util.Base64.getUrlEncoder().encodeToString(
                (encodedHeader + "." + encodedPayload + JWT_SECRET).getBytes());

        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    /**
     * 验证JWT令牌
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new RuntimeException("无效的令牌格式");
            }

            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            // 简单的JSON解析（实际应使用专业JSON库）
            String userIdStr = payload.substring(payload.indexOf("\"userId\":") + 9, payload.indexOf(","));
            String expStr = payload.substring(payload.indexOf("\"exp\":") + 6, payload.indexOf("}"));

            Long userId = Long.parseLong(userIdStr.trim());
            long exp = Long.parseLong(expStr.trim());

            // 检查是否过期
            if (System.currentTimeMillis() > exp) {
                throw new RuntimeException("令牌已过期");
            }

            return userId;
        } catch (Exception e) {
            throw new RuntimeException("令牌验证失败：" + e.getMessage());
        }
    }

    /**
     * 从令牌中获取用户信息
     *
     * @param token JWT令牌
     * @return 用户信息
     */
    public User getUserFromToken(String token) {
        Long userId = validateToken(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}
