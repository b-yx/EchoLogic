package com.example.aicoauthor.controller;

import com.example.aicoauthor.dto.GenerateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.example.aicoauthor.service.AIGenerationService;
import com.example.aicoauthor.dto.GenerateResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AIGenerationController.class)
public class AIGenerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIGenerationService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGenerate() throws Exception {
        GenerateRequest req = new GenerateRequest();
        req.setContents(Arrays.asList("a","b"));
        req.setGoal("test goal");

        when(aiService.generate(org.mockito.Mockito.any()))
                .thenReturn(new GenerateResponse("mock result", System.currentTimeMillis()));

        mockMvc.perform(post("/api/ai/generate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("mock result"));
    }
}
