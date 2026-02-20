package com.example.smart_library.repository;

import com.example.smart_library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮件查询用户
     * 用于找回密码时定位账号
     * @param email
     * @return
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查询用户
     * 用于找回密码定位账号
     * @param phone
     * @return
     */
    Optional<User> findByPhone(String phone);

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查电话号是否已存在
     *
     * @param phone 电话号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 根据角色查询用户列表
     *
     * @param role 用户角色
     * @return 用户列表
     */
    List<User> findByRole(User.UserRole role);

    /**
     * 根据状态查询用户列表
     *
     * @param status 用户状态
     * @return 用户列表
     */
    List<User> findByStatus(User.UserStatus status);

    /**
     * 根据角色和状态查询用户列表（分页）
     *
     * @param role 用户角色
     * @param status 用户状态
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByRoleAndStatus(User.UserRole role, User.UserStatus status, Pageable pageable);

    /**
     * 根据角色查询用户列表（分页）
     *
     * @param role 用户角色
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByRole(User.UserRole role, Pageable pageable);

    /**
     * 根据状态查询用户列表（分页）
     *
     * @param status 用户状态
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);

    /**
     * 查询当前借阅数量未达到上限的用户
     *
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.currentBorrowCount < u.maxBorrowCount AND u.status = 'ACTIVE'")
    List<User> findUsersWithAvailableBorrowSlots();

    /**
     * 根据用户名或邮箱或手机号模糊查询
     *
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.phone LIKE %:keyword% OR u.realName LIKE %:keyword%")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查询所有管理员
     *
     * @return 管理员列表
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.status = 'ACTIVE'")
    List<User> findAllActiveAdmins();

    /**
     * 统计用户总数
     *
     * @param status 用户状态（可选）
     * @return 用户总数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE (:status IS NULL OR u.status = :status)")
    long countByStatus(@Param("status") User.UserStatus status);

    /**
     * 更新用户的最后登录信息
     *
     * @param userId 用户ID
     * @param loginIp 登录IP
     */
    @Query("UPDATE User u SET u.lastLoginAt = CURRENT_TIMESTAMP, u.lastLoginIp = :loginIp WHERE u.id = :userId")
    void updateLastLoginInfo(@Param("userId") Long userId, @Param("loginIp") String loginIp);

    /**
     * 更新用户的当前借阅数量
     *
     * @param userId 用户ID
     * @param change 变化量
     */
    @Query("UPDATE User u SET u.currentBorrowCount = u.currentBorrowCount + :change WHERE u.id = :userId")
    void updateCurrentBorrowCount(@Param("userId") Long userId, @Param("change") Integer change);

    /**
     * 查询借阅数量超标的用户
     *
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.currentBorrowCount > u.maxBorrowCount")
    List<User> findUsersWithOverdueBorrowLimit();


}
