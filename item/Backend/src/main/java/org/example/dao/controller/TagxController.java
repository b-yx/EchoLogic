package org.example.dao.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dao.pojo.Tagx;
import org.example.dao.service.TagxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@Tag(name = "标签管理", description = "标签的增删改查操作")
public class TagxController {

    @Autowired
    private TagxService tagService;

    // 获取所有标签
    @GetMapping
    @Operation(summary = "获取所有标签", description = "查询系统中的所有标签")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Tagx.class)))
    })
    public List<Tagx> getAllTags() {
        return tagService.findAll();
    }

    // 根据ID获取标签
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取标签", description = "根据指定的ID查询标签详情")
    @Parameters(value = {
            @Parameter(name = "id", description = "标签ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Tagx.class))),
            @ApiResponse(responseCode = "404", description = "标签不存在")
    })
    public Tagx getTagById(@PathVariable Integer id) {
        return tagService.findById(id);
    }

    // 创建标签
    @PostMapping
    @Operation(summary = "创建标签", description = "创建一个新的标签，标签名称必须唯一")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误或标签名称已存在")
    })
    public void createTag(@RequestBody 
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                description = "标签信息", 
                                required = true,
                                content = @Content(schema = @Schema(implementation = Tagx.class)))
                            Tagx tag) {
        tagService.createTag(tag);
    }

    // 添加一个测试接口，用于验证API是否正常工作
    @GetMapping("/test")
    public ResponseEntity<String> testAPI() {
        System.out.println("测试接口被调用");
        return ResponseEntity.ok("API 正常工作");
    }

    // 更新标签 - 使用RequestEntity<String>获取原始请求体，以便直接检查JSON数据
    @PutMapping("/{id}")
    @Operation(summary = "更新标签", description = "更新指定ID的标签信息")
    @Parameters(value = {
            @Parameter(name = "id", description = "标签ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误或标签名称已存在"),
            @ApiResponse(responseCode = "404", description = "标签不存在")
    })
    public ResponseEntity<?> updateTag(@PathVariable Integer id, 
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                description = "标签信息", 
                                required = true,
                                content = @Content(mediaType = "application/json"))
                            RequestEntity<String> requestEntity) {
        try {
            // 详细日志记录
            System.out.println("===== 更新标签请求开始 =====");
            System.out.println("标签ID: " + id);
            
            // 获取原始请求体
            String requestBody = requestEntity.getBody();
            System.out.println("原始请求体内容: " + requestBody);
            
            // 验证请求体不为空
            if (requestBody == null || requestBody.trim().isEmpty()) {
                System.out.println("请求体为空，验证失败");
                return ResponseEntity.badRequest().body("请求数据不能为空");
            }
            
            // 使用Jackson手动解析JSON
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> requestData = objectMapper.readValue(requestBody, Map.class);
            
            // 打印解析后的请求数据
            System.out.println("解析后的请求数据: " + requestData);
            System.out.println("请求数据键集合: " + requestData.keySet());
            
            // 验证标签名称不为空
            String name = (String) requestData.get("name");
            System.out.println("解析的name值: " + (name != null ? "'" + name + "'" : "null"));
            
            if (name == null || name.trim().isEmpty()) {
                System.out.println("标签名称为空或无效，验证失败");
                return ResponseEntity.badRequest().body("标签名称不能为空");
            }
            
            // 验证标签是否存在
            System.out.println("验证标签是否存在，ID: " + id);
            Tagx existingTag = tagService.findById(id);
            if (existingTag == null) {
                System.out.println("标签不存在，ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            // 创建要更新的标签对象
            Tagx tagToUpdate = new Tagx();
            tagToUpdate.setId(id);
            tagToUpdate.setName(name.trim());
            
            // 处理color字段
            String color = (String) requestData.get("color");
            System.out.println("解析的color值: " + (color != null ? "'" + color + "'" : "null"));
            
            if (color != null && !color.trim().isEmpty()) {
                tagToUpdate.setColor(color.trim());
            } else {
                tagToUpdate.setColor(existingTag.getColor());
                System.out.println("color为空，使用现有color值: " + existingTag.getColor());
            }
            
            System.out.println("准备更新的标签对象: " + tagToUpdate);
            
            // 执行更新
            System.out.println("执行标签更新");
            tagService.updateTag(tagToUpdate);
            System.out.println("标签更新成功");
            
            System.out.println("===== 更新标签请求结束 =====");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("===== 更新标签时发生错误 =====");
            System.err.println("错误类型: " + e.getClass().getName());
            System.err.println("错误消息: " + e.getMessage());
            System.err.println("完整堆栈:");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("更新标签失败: " + e.getMessage());
        }
    }

    // 删除标签
    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签", description = "删除指定ID的标签")
    @Parameters(value = {
            @Parameter(name = "id", description = "标签ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "标签不存在")
    })
    public void deleteTag(@PathVariable Integer id) {
        tagService.deleteTag(id);
    }

    // 根据记录ID获取标签列表
    @GetMapping("/record/{recordId}")
    @Operation(summary = "根据记录ID获取标签", description = "查询指定记录关联的所有标签")
    @Parameters(value = {
            @Parameter(name = "recordId", description = "记录ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Tagx.class))),
            @ApiResponse(responseCode = "404", description = "记录不存在")
    })
    public List<Tagx> getTagsByRecordId(@PathVariable Integer recordId) {
        return tagService.findTagsByRecordId(recordId);
    }
}