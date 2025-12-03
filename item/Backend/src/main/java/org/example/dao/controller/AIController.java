package org.example.dao.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dao.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI对话", description = "AI对话相关接口")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/chat")
    @Operation(summary = "AI对话", description = "与AI进行对话，支持上下文记忆")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "对话成功"),
            @ApiResponse(responseCode = "500", description = "AI服务错误")
    })
    public ResponseEntity<Map<String, Object>> chat(
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "对话历史",
                    required = true,
                    content = @Content(mediaType = "application/json")
            ) List<Map<String, String>> conversationHistory) {
        try {
            String aiResponse = aiService.chatWithAI(conversationHistory);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", aiResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "对话失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}