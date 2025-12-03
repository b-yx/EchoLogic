package org.example.dao.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.dao.pojo.Record;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

@Service
public class AIService {

    private final WebClient webClient;

    // 注入WebClient
    @Autowired
    public AIService(WebClient webClient) {
        this.webClient = webClient;
    }

    // ----------- 核心方法：调用 DeepSeek API -----------
    private String callDeepSeekApi(String prompt, String text) {
        // 使用DeepSeek-Coder模型或其他Chat模型
        String model = "deepseek-chat";

        // DeepSeek/OpenAI API 的标准请求体结构
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", prompt));
        messages.add(Map.of("role", "user", "content", text));
        
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", messages,
                "temperature", 0.3 // 保持较低温度以获取稳定结果
        );

        // 发送请求
        Mono<Map> responseMono = webClient.post()
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    System.err.println("调用 DeepSeek API 失败: " + e.getMessage());
                    // 返回一个包含默认错误信息的结构，防止后续代码解析崩溃
                    return Mono.just(Map.of("choices", List.of(
                            Map.of("message", Map.of("content", "[]"))
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
            return "[]";
        }
    }

    // AI分析用户描述并生成筛选条件的核心方法
    public List<Integer> analyzeDescriptionAndFilterRecords(String description, List<Record> allRecords) {
        // 将记录列表转换为JSON格式，方便AI处理
        StringBuilder recordsJson = new StringBuilder("[");
        for (int i = 0; i < allRecords.size(); i++) {
            Record record = allRecords.get(i);
            recordsJson.append(String.format(
                    "{\"id\":%d,\"title\":\"%s\",\"content\":\"%s\",\"contentType\":\"%s\"}",
                    record.getId(),
                    escapeJson(record.getTitle()),
                    escapeJson(record.getContent()),
                    record.getContentType()
            ));
            if (i < allRecords.size() - 1) {
                recordsJson.append(",");
            }
        }
        recordsJson.append("]");

        // 构建AI提示词
        String prompt = "你是一个专业的数据分析师，请根据用户提供的描述，从给定的记录列表中筛选出符合条件的记录。" +
                "请仅返回匹配的记录ID列表，格式为JSON数组，例如：[1,3,5]。" +
                "如果没有匹配的记录，请返回空数组[]。" +
                "不要返回任何解释或额外信息，只返回JSON数组。";

        // 调用AI API进行分析
        System.out.println("=== AI调用调试信息 ===");
        System.out.println("描述：" + description);
        System.out.println("记录数量：" + allRecords.size());
        System.out.println("记录列表（部分）：" + (recordsJson.length() > 100 ? recordsJson.substring(0, 100) + "..." : recordsJson));
        
        String aiResponse = callDeepSeekApi(prompt, "描述：" + description + "\n\n记录列表：" + recordsJson);
        
        System.out.println("AI原始响应：" + aiResponse);

        try {
            // 使用JSON库进行更可靠的解析
            ObjectMapper mapper = new ObjectMapper();
            List<Integer> matchingRecordIds = mapper.readValue(aiResponse, new TypeReference<List<Integer>>() {});
            System.out.println("解析后的记录ID列表：" + matchingRecordIds);
            return matchingRecordIds;
        } catch (Exception e) {
            System.err.println("解析AI响应失败: " + e.getMessage());
            System.err.println("尝试备用解析方法...");
            
            // 备用解析方法：移除可能的换行符和空格
            String cleanedResponse = aiResponse.replaceAll("[\\r\\n\\s]", "");
            
            // 尝试提取JSON数组部分
            int startIndex = cleanedResponse.indexOf("[");
            int endIndex = cleanedResponse.lastIndexOf("]");
            
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                cleanedResponse = cleanedResponse.substring(startIndex, endIndex + 1);
                
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Integer> matchingRecordIds = mapper.readValue(cleanedResponse, new TypeReference<List<Integer>>() {});
                    System.out.println("备用解析成功，记录ID列表：" + matchingRecordIds);
                    return matchingRecordIds;
                } catch (Exception ex) {
                    System.err.println("备用解析也失败: " + ex.getMessage());
                    return Collections.emptyList();
                }
            } else {
                return Collections.emptyList();
            }
        }
    }

    // 生成集合名称的方法
    public String generateCollectionName(String description) {
        // 构建AI提示词
        String prompt = "请根据用户的描述，生成一个简洁的集合名称，不超过20个字符。" +
                "只返回集合名称，不要添加任何解释或额外信息。";

        // 调用AI API生成名称
        String aiResponse = callDeepSeekApi(prompt, description);

        // 清理并返回结果
        String cleanedName = aiResponse.replaceAll("[\\r\\n\\s]", "");
        if (cleanedName.length() > 20) {
            cleanedName = cleanedName.substring(0, 20) + "...";
        }
        return cleanedName;
    }

    // 处理AI对话的方法，支持上下文记忆
    public String chatWithAI(List<Map<String, String>> conversationHistory) {
        // 构建系统提示词
        String systemPrompt = "你是一个专业的知识库AI助手，帮助用户管理和查询他们的知识库。" +
                "你可以回答用户关于知识库的问题，也可以处理用户的功能性指令。" +
                "对于功能性指令，请按照特定格式返回，例如：" +
                "{\"type\":\"command\",\"command\":\"filter\",\"params\":{\"keyword\":\"xxx\"}}" +
                "如果无法识别为功能性指令，则返回普通对话内容。";

        // 调用DeepSeek API进行对话
        String model = "deepseek-chat";        
        
        // 创建可修改的消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.addAll(conversationHistory);
        
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", messages,
                "temperature", 0.7 // 稍微提高温度，使回复更自然
        );

        // 发送请求
        Mono<Map> responseMono = webClient.post()
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    System.err.println("调用 DeepSeek API 失败: " + e.getMessage());
                    // 返回一个包含默认错误信息的结构，防止后续代码解析崩溃
                    return Mono.just(Map.of("choices", List.of(
                            Map.of("message", Map.of("content", "很抱歉，AI服务暂时不可用。"))
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
            return "很抱歉，我暂时无法理解您的请求。";
        }
    }

    // 辅助方法：转义JSON字符串
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\\n");
    }
}