package com.example.aicoauthor.client;

import org.springframework.stereotype.Component;

@Component
public class AIClient {

    public String call(String prompt) {
        // Mock implementation — 替换为真实 AI API 调用（比如 OpenAI/自己部署的 LLM）
        // 为了测试，我们返回一个固定格式的响应
        String mock = "【AI生成内容】\n" +
                "提示摘要: " + (prompt.length() > 100 ? prompt.substring(0, 100) + "..." : prompt) +
                "\n\n生成建议:\n1. 这是基于所选内容生成的第一条建议\n2. 这是第二条建议\n\n（这是Mock返回，用于本地测试）";
        return mock;
    }
}
