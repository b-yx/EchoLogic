package org.example.dao.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dao.pojo.Record;
import org.example.dao.pojo.Tagx;
import org.example.dao.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
@Tag(name = "记录管理", description = "记录的增删改查及标签关联操作")
public class RecordController {

    @Autowired
    private RecordService recordService;

    // 获取所有记录
    @GetMapping
    @Operation(summary = "获取所有记录", description = "查询系统中的所有记录，按创建时间倒序排列")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Record.class)))
    })
    public List<Record> getAllRecords() {
        return recordService.findAll();
    }

    // 根据ID获取记录
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取记录", description = "根据指定的ID查询记录详情")
    @Parameters(value = {
            @Parameter(name = "id", description = "记录ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Record.class))),
            @ApiResponse(responseCode = "404", description = "记录不存在")
    })
    public Record getRecordById(@PathVariable Integer id) {
        return recordService.findById(id);
    }

    // 根据集合ID获取记录列表
    @GetMapping("/collection/{collectionId}")
    @Operation(summary = "根据集合ID获取记录", description = "查询指定集合下的所有记录")
    @Parameters(value = {
            @Parameter(name = "collectionId", description = "集合ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Record.class)))
    })
    public List<Record> getRecordsByCollectionId(@PathVariable Integer collectionId) {
        return recordService.findByCollectionId(collectionId);
    }

    // 创建新记录
     @PostMapping
    @Operation(summary = "创建新记录", description = "创建一个新的记录")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Record createRecord(@RequestBody Record record) {
        System.out.println("====== [调试开始] 接收到创建请求 ======");
        try {
            // 1. 打印接收到的数据，检查有没有字段是 null
            System.out.println("标题: " + record.getTitle());
            System.out.println("集合ID: " + record.getCollectionId());
            System.out.println("内容类型: " + record.getContentType());
            
            // 2. 手动补全时间 (防止因时间为 null 导致的报错)
            if (record.getCreateTime() == null) record.setCreateTime(new java.util.Date());
            if (record.getUpdateTime() == null) record.setUpdateTime(new java.util.Date());
            
            // 3. 检查 Service 是否注入成功
            if (recordService == null) {
                throw new RuntimeException("严重错误：RecordService 注入失败，为 null！");
            }

            // 4. 执行业务逻辑
            System.out.println("正在调用 Service 保存数据...");
            recordService.createRecord(record);
            
            System.out.println("====== [调试结束] 创建成功，ID: " + record.getId() + " ======");
            return record;

        } catch (Exception e) {
            // 🚨 重点：在这里捕获所有异常并打印出来！
            System.err.println("====== [发生异常] ======");
            e.printStackTrace(); // 这行代码会把报错详情打印在控制台
            System.err.println("异常信息: " + e.getMessage());
            
            // 为了让前端知道具体错误，这里重新抛出
            throw new RuntimeException("创建失败: " + e.getMessage());
        }
    }

    // 更新记录
    @PutMapping("/{id}")
    @Operation(summary = "更新记录", description = "更新指定ID的记录信息")
    @Parameters(value = {
            @Parameter(name = "id", description = "记录ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "404", description = "记录不存在")
    })
    public void updateRecord(@PathVariable Integer id, 
                           @RequestBody
                           @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                   description = "记录信息", 
                                   required = true,
                                   content = @Content(schema = @Schema(implementation = Record.class)))
                           Record record) {
        System.out.println("=== 开始处理更新记录请求 ===");
        System.out.println("接收到的记录ID: " + id);
        System.out.println("接收到的请求体中name: " + (record != null ? record.getTitle() : "null"));
        System.out.println("接收到的请求体中content: " + (record != null ? record.getContent() : "null"));
        record.setId(id);
        recordService.updateRecord(record);
        System.out.println("=== 更新记录请求处理完成 ===");
    }

    // 删除记录
    @DeleteMapping("/{id}")
    @Operation(summary = "删除记录", description = "删除指定ID的记录")
    @Parameters(value = {
            @Parameter(name = "id", description = "记录ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "记录不存在")
    })
    public void deleteRecord(@PathVariable Integer id) {
        recordService.deleteRecord(id);
    }

    // 为记录添加标签
    @PostMapping("/{recordId}/tags/{tagId}")
    @Operation(summary = "为记录添加标签", description = "建立记录和标签之间的关联关系")
    @Parameters(value = {
            @Parameter(name = "recordId", description = "记录ID", required = true),
            @Parameter(name = "tagId", description = "标签ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "添加成功"),
            @ApiResponse(responseCode = "404", description = "记录或标签不存在"),
            @ApiResponse(responseCode = "400", description = "关联关系已存在")
    })
    public void addTagToRecord(@PathVariable Integer recordId, @PathVariable Integer tagId) {
        recordService.addTagToRecord(recordId, tagId);
    }

    // 从记录中移除标签
    @DeleteMapping("/{recordId}/tags/{tagId}")
    @Operation(summary = "从记录中移除标签", description = "解除记录和标签之间的关联关系")
    @Parameters(value = {
            @Parameter(name = "recordId", description = "记录ID", required = true),
            @Parameter(name = "tagId", description = "标签ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "移除成功"),
            @ApiResponse(responseCode = "404", description = "记录或标签不存在")
    })
    public void removeTagFromRecord(@PathVariable Integer recordId, @PathVariable Integer tagId) {
        recordService.removeTagFromRecord(recordId, tagId);
    }

    // 获取记录的所有标签
    @GetMapping("/{recordId}/tags")
    @Operation(summary = "获取记录的所有标签", description = "查询指定记录关联的所有标签")
    @Parameters(value = {
            @Parameter(name = "recordId", description = "记录ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = Tagx.class))),
            @ApiResponse(responseCode = "404", description = "记录不存在")
    })
    public List<Tagx> getRecordTags(@PathVariable Integer recordId) {
        return recordService.findRecordTags(recordId);
    }
    
    // 搜索记录
    @GetMapping("/search")
    @Operation(summary = "搜索记录", description = "根据关键字搜索记录")
    @Parameter(name = "keyword", description = "搜索关键字", required = true)
    @ApiResponse(responseCode = "200", description = "搜索成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Record.class)))
    public List<Record> searchRecords(@RequestParam String keyword) {
        return recordService.searchRecords(keyword);
    }
    
    // 根据类型查询记录
    @GetMapping("/type/{contentType}")
    @Operation(summary = "根据类型查询记录", description = "查询指定类型的所有记录")
    @Parameter(name = "contentType", description = "记录类型", required = true)
    @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Record.class)))
    public List<Record> getRecordsByType(@PathVariable String contentType) {
        return recordService.findByType(contentType);
    }
    
    // 保存草稿
    @PutMapping("/{id}/draft")
    @Operation(summary = "保存草稿", description = "保存记录的草稿内容")
    @Parameters(value = {
            @Parameter(name = "id", description = "记录ID", required = true)
    })
    @ApiResponse(responseCode = "200", description = "保存成功")
    public void saveDraft(@PathVariable Integer id, @RequestBody Map<String, Object> content) {
        recordService.saveDraft(id, content);
    }
    
    // 恢复记录
    @PutMapping("/{id}/restore")
    @Operation(summary = "恢复记录", description = "恢复已删除的记录")
    @Parameter(name = "id", description = "记录ID", required = true)
    @ApiResponse(responseCode = "200", description = "恢复成功")
    public void restoreRecord(@PathVariable Integer id) {
        recordService.restoreRecord(id);
    }
    
    // 生成标签推荐
    @GetMapping("/{id}/tag-recommendations")
    @Operation(summary = "生成标签推荐", description = "根据记录内容生成标签推荐")
    @Parameter(name = "id", description = "记录ID", required = true)
    @ApiResponse(responseCode = "200", description = "推荐成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Tagx.class)))
    public List<Tagx> generateTagRecommendations(@PathVariable Integer id) {
        return recordService.generateTagRecommendations(id);
    }
}