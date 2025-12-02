package com.example.smartparse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AIService {

    private final WebClient webClient;

    // 从 application.properties 读取配置

    // 构造函数注入 WebClient 并设置认证
    public AIService(WebClient webClient) {
        this.webClient = webClient;
    }

    // ----------- 核心方法：调用 DeepSeek API -----------
    private String callDeepSeekApi(String prompt, String text) {
        // 推荐使用 DeepSeek-Coder 模型或其他 Chat 模型
        String model = "deepseek-chat";

        // DeepSeek/OpenAI API 的标准请求体结构
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", prompt),
                        Map.of("role", "user", "content", text)
                ),
                "temperature", 0.3 // 保持较低温度以获取稳定总结
        );

        // 发送请求，注意这里使用相对路径 "/" 或空字符串，因为我们在调用时会使用全路径
        Mono<Map> responseMono = webClient.post()
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    System.err.println("调用 DeepSeek API 失败: " + e.getMessage());
                    // 返回一个包含默认错误信息的结构，防止后续代码解析崩溃
                    return Mono.just(Map.of("choices", List.of(
                            Map.of("message", Map.of("content", "调用失败，请检查网络或API Key。"))
                    )));
                });

        // 阻塞获取结果，设置超时时间，防止长时间等待
        Map result = responseMono.block(java.time.Duration.ofSeconds(30));

        try {
            // 提取结果：choices[0].message.content
            List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            System.err.println("解析 DeepSeek 响应失败: " + e.getMessage());
            return "API 响应解析失败。";
        }
    }

    // ----------- 实现 generateSummary (摘要) -----------
    public String generateSummary(String text) {
        String prompt = "你是一个专业的总结分析师，请将用户提供的文本内容总结为100字以内的中文摘要，聚焦核心观点和价值。只输出摘要内容，不要有任何多余的解释。";
        String summary = callDeepSeekApi(prompt, text);
        return "[DeepSeek AI 摘要]: " + summary;
    }

    // ----------- 实现 generateTags (标签) -----------
    public String generateTags(String text) {
        String prompt = "你是一个专业的标签提取器，请从用户提供的文本中提取3到5个最具代表性的技术关键词作为标签。标签之间用逗号分隔，不要有空格。只输出标签字符串，不要有任何多余的解释。";
        String tags = callDeepSeekApi(prompt, text);

        // 清理DeepSeek可能多余的标点符号或换行
        tags = tags.replace("\n", "").trim();

        // 保证失败时有默认值
        if (tags.length() < 5 || tags.contains("失败")) {
            return "AI, 科技, 知识管理";
        }

        return tags;
    }

    // 媒体总结保持模拟
    public String summarizeMedia(String type) {
        // ... (保持不变)
        if ("audio".equals(type)) {
            return "[AI听觉]: 这段音频内容经过AI语音识别处理，识别出重点讨论了系统设计模式的优势与挑战。";
        } else if ("video".equals(type)) {
            return "[AI视觉]: 视频经过帧分析和语音转文本，总结出核心功能的操作流程和前端优化细节。";
        }
        return "未知媒体内容";
    }
}