package com.example.smart_library.service;

import com.example.smart_library.dto.request.RegisterRequest;
import com.example.smart_library.dto.response.UserInfoResponse;
import com.example.smart_library.entity.User;
import com.example.smart_library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务接口实现
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 用户信息
     */
    @Transactional
    public UserInfoResponse register(RegisterRequest request) {
        // 校验密码确认
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 检查手机号是否已存在
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole(User.UserRole.READER);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setMaxBorrowCount(5);
        user.setCurrentBorrowCount(0);

        user = userRepository.save(user);

        return convertToResponse(user);
    }

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public UserInfoResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToResponse(user);
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    public UserInfoResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToResponse(user);
    }

    /**
     * 更新用户信息
     *
     * @param userId  用户ID
     * @param user    用户信息
     * @return 更新后的用户信息
     */
    @Transactional
    public UserInfoResponse updateUser(Long userId, User user) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 更新允许修改的字段
        if (user.getRealName() != null) {
            existingUser.setRealName(user.getRealName());
        }
        if (user.getPhone() != null) {
            // 检查手机号是否被其他用户使用
            if (!existingUser.getPhone().equals(user.getPhone()) &&
                    userRepository.existsByPhone(user.getPhone())) {
                throw new RuntimeException("手机号已被其他用户使用");
            }
            existingUser.setPhone(user.getPhone());
        }
        if (user.getEmail() != null) {
            // 检查邮箱是否被其他用户使用
            if (!existingUser.getEmail().equals(user.getEmail()) &&
                    userRepository.existsByEmail(user.getEmail())) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
            existingUser.setEmail(user.getEmail());
        }

        existingUser = userRepository.save(existingUser);
        return convertToResponse(existingUser);
    }

    /**
     * 增加用户借阅数量
     *
     * @param userId 用户ID
     */
    @Transactional
    public void increaseBorrowCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getCurrentBorrowCount() >= user.getMaxBorrowCount()) {
            throw new RuntimeException("已达到最大借阅数量");
        }

        user.setCurrentBorrowCount(user.getCurrentBorrowCount() + 1);
        userRepository.save(user);
    }

    /**
     * 减少用户借阅数量
     *
     * @param userId 用户ID
     */
    @Transactional
    public void decreaseBorrowCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (user.getCurrentBorrowCount() <= 0) {
            throw new RuntimeException("当前借阅数量不能为负数");
        }

        user.setCurrentBorrowCount(user.getCurrentBorrowCount() - 1);
        userRepository.save(user);
    }

    /**
     * 更新最后登录信息
     *
     * @param userId 用户ID
     * @param ip     登录IP
     */
    @Transactional
    public void updateLastLogin(Long userId, String ip) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setLastLoginAt(java.time.LocalDateTime.now());
        user.setLastLoginIp(ip);
        userRepository.save(user);
    }

    /**
     * 检查用户是否可以借书
     *
     * @param userId 用户ID
     * @return 是否可以借书
     */
    public boolean canBorrow(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return user.getStatus() == User.UserStatus.ACTIVE &&
                user.getCurrentBorrowCount() < user.getMaxBorrowCount();
    }

    /**
     * 分页查询所有用户
     *
     * @param pageable 分页参数
     * @return 用户列表
     */
    public Page<UserInfoResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    /**
     * 将User实体转换为UserInfoResponse
     *
     * @param user 用户实体
     * @return 用户信息响应
     */
    private UserInfoResponse convertToResponse(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole().name())
                .roleDescription(user.getRole().getDescription())
                .status(user.getStatus().name())
                .statusDescription(user.getStatus().getDescription())
                .maxBorrowCount(user.getMaxBorrowCount())
                .currentBorrowCount(user.getCurrentBorrowCount())
                .availableBorrowCount(user.getMaxBorrowCount() - user.getCurrentBorrowCount())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
