package com.example.smart_library.service;

import com.example.smart_library.common.BusinessException;
import com.example.smart_library.entity.User;
import com.example.smart_library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * 由id查询用户
     * @param userId
     * @return
     */
    public User findByUserId(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在！"));
    }

    /**
     * 由用户名查询用户
     * @param username
     * @return
     */
    public User FindByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在！"));
    }

    public Optional<User> FindByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public Optional<User> FindByPhone(String phone){
        return userRepository.findByPhone(phone);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email){
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhone(String phone){
        return userRepository.existsByPhone(phone);
    }

    public Page<User> getUserList(Pageable pageable){
        return userRepository.findAll(pageable);
    }
}
