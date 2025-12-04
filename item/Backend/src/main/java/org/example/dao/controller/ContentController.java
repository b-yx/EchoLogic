package org.example.dao.controller;

import org.example.dao.dto.GenerateRequest;
import org.example.dao.dto.GenerateResponse;
import org.example.dao.dto.RefineRequest;
import org.example.dao.dto.ParseUrlRequest;
import org.example.dao.pojo.WebContent;
import org.example.dao.service.AIGenerationService;
import org.example.dao.service.WebParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.IOException;

@RestController
@RequestMapping("/api/content")
@Tag(name = "内容管理", description = "提供网页解析、AI内容生成和优化功能")
public class ContentController {

    @Autowired
    private WebParseService webParseService;

    @Autowired
    private AIGenerationService aiGenerationService;

    /**
     * 解析网页并保存为收藏
     */
    @Operation(
            summary = "解析网页",
            description = "根据提供的URL解析网页内容，并将解析结果保存为收藏",
            tags = { "内容管理" }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "网页解析成功", content = @Content(schema = @Schema(implementation = WebContent.class))),
            @ApiResponse(responseCode = "500", description = "网页解析失败")
    })
    @PostMapping("/parse-url")
    public ResponseEntity<?> parseUrl(@RequestBody ParseUrlRequest request) {
        try {
            WebContent webContent = webParseService.parseAndSave(request.getUrl());
            return ResponseEntity.ok(webContent);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("解析网页失败: " + e.getMessage());
        }
    }



    /**
     * AI内容生成
     */
    @Operation(
            summary = "AI内容生成",
            description = "根据提供的提示生成新的内容",
            tags = { "内容管理" }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "内容生成成功", content = @Content(schema = @Schema(implementation = GenerateResponse.class))),
            @ApiResponse(responseCode = "500", description = "内容生成失败")
    })
    @PostMapping("/generate")
    public ResponseEntity<GenerateResponse> generate(@RequestBody GenerateRequest req) {
        try {
            GenerateResponse response = aiGenerationService.generate(req);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenerateResponse("生成内容失败: " + e.getMessage(), System.currentTimeMillis()));
        }
    }

    /**
     * 内容优化
     */
    @Operation(
            summary = "内容优化",
            description = "优化现有的内容，提升质量",
            tags = { "内容管理" }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "内容优化成功", content = @Content(schema = @Schema(implementation = GenerateResponse.class))),
            @ApiResponse(responseCode = "500", description = "内容优化失败")
    })
    @PostMapping("/refine")
    public ResponseEntity<GenerateResponse> refine(@RequestBody RefineRequest req) {
        try {
            GenerateResponse response = aiGenerationService.refine(req);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenerateResponse("优化内容失败: " + e.getMessage(), System.currentTimeMillis()));
        }
    }
}