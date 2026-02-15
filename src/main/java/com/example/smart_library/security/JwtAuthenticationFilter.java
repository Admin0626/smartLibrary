package com.example.smart_library.security;

import com.example.smart_library.common.BusinessException;
import com.example.smart_library.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 * 拦截请求，验证JWT token的有效性
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Component
public class JwtAuthenticationFilter implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 白名单路径（不需要验证token的路径）
     */
    private static final String[] WHITE_LIST = {
            "/api/auth/login",           // 登录
            "/api/auth/register",        // 注册
            "/api/auth/send-code",       // 发送验证码
            "/api/auth/reset-password",  // 重置密码
            "/api/books",                // 图书列表（允许未登录浏览）
            "/api/books/search",         // 搜索图书
            "/api/books/hot",            // 热门图书
            "/api/books/recommend",      // 推荐图书
            "/api/announcements",        // 公告列表
            "/swagger-ui",               // Swagger UI
            "/v3/api-docs"               // API文档
    };

    /**
     * 请求处理前执行
     *
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @return true-放行，false-拦截
     * @throws Exception 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestUri = request.getRequestURI();

        // 判断是否在白名单中
        if (isWhiteList(requestUri)) {
            log.debug("请求路径在白名单中，放行：{}", requestUri);
            return true;
        }

        // 从请求头获取Authorization
        String authHeader = request.getHeader("Authorization");

        // 检查Authorization是否存在
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("请求头中缺少Authorization或格式错误：{}", requestUri);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未授权，请先登录\"}");
            return false;
        }

        // 提取token字符串
        String token = authHeader.substring(7);  // 去掉"Bearer "前缀

        // 验证token
        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("Token无效或已过期：{}", requestUri);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期，请重新登录\"}");
                return false;
            }

            // 解析token，提取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            log.debug("Token验证成功，用户ID：{}，用户名：{}，角色：{}", userId, username, role);

            // 将用户信息存入request属性，供Controller使用
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);

            return true;  // 放行

        } catch (Exception e) {
            log.error("Token解析失败：{}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token解析失败，请重新登录\"}");
            return false;
        }
    }

    /**
     * 判断请求路径是否在白名单中
     *
     * @param requestUri 请求路径
     * @return true-在白名单中，false-不在白名单中
     */
    private boolean isWhiteList(String requestUri) {
        for (String whitePath : WHITE_LIST) {
            if (requestUri.startsWith(whitePath)) {
                return true;
            }
        }
        return false;
    }
}
