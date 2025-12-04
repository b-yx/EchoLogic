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
        String prompt = 
                "以下是用户之前生成的内容，请继续深化、扩展或调整：\n" +
                "【当前内容】\n" + req.getCurrentContent() +
                "\n\n【用户新指令】\n" + req.getInstruction();

        String output = aiService.callDeepSeekApi(prompt, null, null, null);
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