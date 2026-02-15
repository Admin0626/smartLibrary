package com.example.smart_library.config;

import com.example.smart_library.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 注册拦截器和配置CORS跨域
 *
 * @author SmartLibrary
 * @since 2024-02-15
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 注册拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationFilter)
                .addPathPatterns("/api/**")              // 拦截所有API请求
                .excludePathPatterns(                   // 放行路径（白名单）
                        "/api/auth/login",              // 登录
                        "/api/auth/register",           // 注册
                        "/api/auth/send-code",          // 发送验证码
                        "/api/auth/reset-password",     // 重置密码
                        "/api/books",                   // 图书列表
                        "/api/books/search",            // 搜索图书
                        "/api/books/hot",               // 热门图书
                        "/api/books/recommend",         // 推荐图书
                        "/api/announcements",           // 公告列表
                        "/swagger-ui/**",               // Swagger UI
                        "/v3/api-docs/**"               // API文档
                );
    }

    /**
     * 配置CORS跨域
     *
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                      // 允许跨域的路径
                .allowedOrigins("*")                    // 允许的源（开发环境使用*）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的HTTP方法
                .allowedHeaders("*")                    // 允许的请求头
                .allowCredentials(true)                 // 允许携带凭证
                .maxAge(3600);                          // 预检请求的缓存时间（秒）
    }
}
