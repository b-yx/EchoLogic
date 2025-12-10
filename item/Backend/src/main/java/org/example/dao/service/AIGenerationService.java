package org.example.dao.service;

import org.example.dao.dto.GenerateRequest;
import org.example.dao.dto.GenerateResponse;
import org.example.dao.dto.RefineRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIGenerationService {

    @Autowired
    private AIService aiService;

    public GenerateResponse generate(GenerateRequest req) {
        String prompt = buildPrompt(req.getContents(), req.getGoal());
        String output = aiService.callDeepSeekApi(prompt, null, null, null);
        return new GenerateResponse(output, System.currentTimeMillis());
    }

    public GenerateResponse refine(RefineRequest req) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个智能助手，能够与用户进行多轮对话，基于之前的对话内容继续深化、扩展或调整想法。\n");
        
        // 添加对话历史
        if (req.getDialogueHistory() != null && !req.getDialogueHistory().isEmpty()) {
            prompt.append("【对话历史】\n");
            for (java.util.Map<String, String> message : req.getDialogueHistory()) {
                String role = message.get("role");
                String content = message.get("content");
                prompt.append(role.equals("user") ? "用户：" : "助手：").append(content).append("\n");
            }
        }
        
        // 添加当前内容和用户新指令
        if (req.getCurrentContent() != null && !req.getCurrentContent().isEmpty()) {
            prompt.append("\n【当前内容】\n").append(req.getCurrentContent()).append("\n");
        }
        
        prompt.append("\n【用户新指令】\n").append(req.getInstruction());

        String output = aiService.callDeepSeekApi(prompt.toString(), null, null, null);
        return new GenerateResponse(output, System.currentTimeMillis());
    }

    private String buildPrompt(List<String> contents, String goal) {
        StringBuilder sb = new StringBuilder();
        sb.append("以下是用户选定的多个收藏内容，请基于它们进行智能创作：\n");
        if (contents != null) {
            for (int i = 0; i < contents.size(); i++) {
                sb.append("内容 ").append(i + 1).append(": ").append(contents.get(i)).append("\n");
            }
        }
        sb.append("\n创作目标：").append(goal == null ? "" : goal);
        return sb.toString();
    }
}