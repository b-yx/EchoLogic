package org.example.dao.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dao.dto.QuickCaptureRequest;
import org.example.dao.pojo.Record;
import org.example.dao.pojo.Tagx;
import org.example.dao.service.RecordService;
import org.example.dao.service.UserBehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
@Tag(name = "记录管理", description = "记录的增删改查及标签关联操作")
public class RecordController {

    @Autowired
    private RecordService recordService;

    @Autowired
    private UserBehaviorService userBehaviorService;

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
        Record record = recordService.findById(id);
        userBehaviorService.recordBehavior(1L, org.example.dao.pojo.UserBehavior.BehaviorType.VIEW, id.longValue());
        return record;
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
    public Record createRecord(@RequestBody Map<String, Object> requestBody) {
        System.out.println("====== [调试开始] 接收到创建请求 ======");
        try {
            // 1. 创建Record对象
            Record record = new Record();
            record.setTitle((String) requestBody.get("title"));
            record.setContent((String) requestBody.get("content"));
            
            // 2. 设置内容类型
            String contentType = (String) requestBody.get("contentType");
            System.out.println("接收到的内容类型: " + contentType);
            record.setContentType(contentType != null ? contentType : "TEXT");
            
            // 3. 设置时间
            Date now = new Date();
            record.setCreateTime(now);
            record.setUpdateTime(now);
            
            // 4. 检查Service是否注入成功
            if (recordService == null) {
                throw new RuntimeException("严重错误：RecordService 注入失败，为 null！");
            }

            // 5. 执行业务逻辑保存记录
            System.out.println("正在调用 Service 保存数据...");
            recordService.createRecord(record);
            
            // 6. 处理标签关联
            List<Integer> tagIds = (List<Integer>) requestBody.get("tagIds");
            if (tagIds != null && !tagIds.isEmpty()) {
                System.out.println("接收到的标签ID: " + tagIds);
                recordService.addTagsToRecord(record.getId(), tagIds);
            }
            
            // 7. 返回保存的记录
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
        userBehaviorService.recordBehavior(1L, org.example.dao.pojo.UserBehavior.BehaviorType.EDIT, id.longValue());
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

    @PostMapping("/quick-capture")
    @Operation(summary = "快速收藏/采集", description = "支持文本输入、拖拽URL、拖拽文件路径")
    @ApiResponse(responseCode = "200", description = "创建成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Record.class)))
    public Record quickCapture(@RequestBody QuickCaptureRequest request) {
        return recordService.quickCapture(
                request.getType(),
                request.getText(),
                request.getUrl(),
                request.getFilePath(),
                request.getTitle(),
                request.getTagIds()
        );
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

    @GetMapping("/{id}/recommendations")
    @Operation(summary = "推荐相似记录", description = "基于标签重合，推荐相似内容")
    @Parameter(name = "id", description = "记录ID", required = true)
    @ApiResponse(responseCode = "200", description = "推荐成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Record.class)))
    public List<Record> recommendSimilar(@PathVariable Integer id) {
        return recordService.recommendSimilar(id);
    }
}
