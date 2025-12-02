package org.example.dao.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，用于配置Spring MVC的拦截器和CORS等组件
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor requestLoggingInterceptor;

    @Autowired
    public WebConfig(RequestLoggingInterceptor requestLoggingInterceptor) {
        this.requestLoggingInterceptor = requestLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册请求日志拦截器，拦截所有API请求
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/api/**") // 只拦截API路径
                .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**"); // 排除Swagger相关路径
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置CORS，允许前端访问后端API
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173") // 允许前端源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(true); // 允许携带凭证
    }
}
