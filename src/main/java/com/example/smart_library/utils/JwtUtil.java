package com.example.smart_library.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 * 负责生成和解析JWT token
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Component
public class JwtUtil {

    /**
     * JWT密钥
     * 从application.yml中读取，如果未配置则使用默认值
     */
    @Value("${jwt.secret:SmartLibrary2024SecretKeyForJWTTokenGeneration}")
    private String secret;

    /**
     * JWT过期时间（毫秒）
     * 从application.yml中读取，默认24小时（86400000毫秒）
     */
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    /**
     * 生成JWT token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return JWT token字符串
     */
    public String generateToken(Long userId, String username, String role) {
        // 设置过期时间
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        // 构建JWT
        return Jwts.builder()
                .setSubject(username)                           // 设置主题（用户名）
                .claim("userId", userId)                        // 添加用户ID
                .claim("role", role)                            // 添加角色
                .setIssuedAt(now)                               // 设置签发时间
                .setExpiration(expiryDate)                      // 设置过期时间
                .signWith(generateSecretKey(), SignatureAlgorithm.HS512) // 使用HS512算法签名
                .compact();
    }

    /**
     * 解析JWT token
     *
     * @param token JWT token字符串
     * @return Claims对象（包含token中的所有信息）
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(generateSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从token中提取用户ID
     *
     * @param token JWT token字符串
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从token中提取用户名
     *
     * @param token JWT token字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从token中提取角色
     *
     * @param token JWT token字符串
     * @return 用户角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 验证token是否有效
     *
     * @param token JWT token字符串
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);  // 尝试解析token
            return true;        // 解析成功，token有效
        } catch (Exception e) {
            return false;       // 解析失败，token无效
        }
    }

    /**
     * 生成签名密钥
     * 将字符串密钥转换为SecretKey对象
     *
     * @return SecretKey对象
     */
    private SecretKey generateSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
