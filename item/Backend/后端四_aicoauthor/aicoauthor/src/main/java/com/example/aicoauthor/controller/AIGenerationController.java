package com.example.aicoauthor.controller;

import com.example.aicoauthor.dto.GenerateRequest;
import com.example.aicoauthor.dto.GenerateResponse;
import com.example.aicoauthor.dto.RefineRequest;
import com.example.aicoauthor.service.AIGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIGenerationController {

    @Autowired
    private AIGenerationService aiService;

    @PostMapping("/generate")
    public ResponseEntity<GenerateResponse> generate(@RequestBody GenerateRequest request) {
        return ResponseEntity.ok(aiService.generate(request));
    }

    @PostMapping("/refine")
    public ResponseEntity<GenerateResponse> refine(@RequestBody RefineRequest request) {
        return ResponseEntity.ok(aiService.refine(request));
    }
}
