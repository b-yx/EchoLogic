package org.example.dao.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;

/**
 * 请求日志拦截器，用于记录HTTP请求和响应的详细信息
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final ThreadLocal<Long> startTimeThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> requestIdThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 生成请求ID和记录开始时间
        String requestId = generateRequestId();
        requestIdThreadLocal.set(requestId);
        startTimeThreadLocal.set(System.currentTimeMillis());
        
        // 记录请求信息
        logger.info("[{}] 请求开始: {} {}, IP: {}, 时间: {}", 
                requestId, 
                request.getMethod(), 
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // 记录请求头
        logHeaders(request);
        
        // 记录请求参数（GET请求）
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            logger.info("[{}] 请求参数: {}", requestId, request.getQueryString());
        }
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // postHandle在响应处理完成后但在视图渲染前调用
        // 这里可以添加额外的处理逻辑，但通常不在此记录响应信息
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 计算请求处理时间
        long startTime = startTimeThreadLocal.get();
        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;
        
        String requestId = requestIdThreadLocal.get();
        
        // 记录响应信息
        logger.info("[{}] 请求结束: {} {}, 状态码: {}, 处理时间: {}ms", 
                requestId, 
                request.getMethod(), 
                request.getRequestURI(),
                response.getStatus(),
                processingTime);
        
        // 记录异常信息（如果有）
        if (ex != null) {
            logger.error("[{}] 请求处理异常: {}", requestId, ex.getMessage(), ex);
        }
        
        // 清理ThreadLocal，避免内存泄漏
        startTimeThreadLocal.remove();
        requestIdThreadLocal.remove();
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return "REQ-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }

    /**
     * 记录请求头信息
     */
    private void logHeaders(HttpServletRequest request) {
        String requestId = requestIdThreadLocal.get();
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            // 敏感信息不记录
            if (!headerName.toLowerCase().contains("authorization") && 
                !headerName.toLowerCase().contains("token")) {
                headers.append(headerName).append(": ").append(headerValue).append(", ");
            }
        }
        if (headers.length() > 0) {
            headers.setLength(headers.length() - 2); // 移除最后的逗号和空格
        }
        logger.info("[{}] 请求头: {}", requestId, headers.toString());
    }
}
