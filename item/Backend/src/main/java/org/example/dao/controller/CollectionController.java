package org.example.dao.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dao.pojo.CollectionEntity;
import org.example.dao.pojo.Record;
import org.example.dao.pojo.RecordCollection;
import org.example.dao.pojo.MergeCollectionsRequest;
import org.example.dao.mapper.RecordCollectionMapper;
import org.example.dao.service.CollectionService;
import org.example.dao.service.RecordService;
import org.example.dao.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/collections")
@Tag(name = "集合管理", description = "集合的增删改查操作")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;
    
    @Autowired
    private RecordService recordService;
    
    @Autowired
    private AIService aiService;
    
    @Autowired
    private RecordCollectionMapper recordCollectionMapper;

    // 获取所有集合
    @GetMapping
    @Operation(summary = "获取所有集合", description = "查询系统中的所有集合，按更新时间倒序排列")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = CollectionEntity.class)))
    })
    public List<CollectionEntity> getAllCollections() {
        return collectionService.findAll();
    }

    // 根据ID获取集合
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取集合", description = "根据指定的ID查询集合详情")
    @Parameters(value = {
            @Parameter(name = "id", description = "集合ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功", 
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = CollectionEntity.class))),
            @ApiResponse(responseCode = "404", description = "集合不存在")
    })
    public CollectionEntity getCollectionById(@PathVariable Integer id) {
        return collectionService.findById(id);
    }

    // 创建新集合
    @PostMapping
    @Operation(summary = "创建新集合", description = "创建一个新的集合")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public void createCollection(@RequestBody 
                               @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                       description = "集合信息", 
                                       required = true,
                                       content = @Content(schema = @Schema(implementation = CollectionEntity.class)))
                               CollectionEntity collection) {
        collectionService.createCollection(collection);
    }

    // 更新集合
    @PutMapping("/{id}")
    @Operation(summary = "更新集合", description = "更新指定ID的集合信息")
    @Parameters(value = {
            @Parameter(name = "id", description = "集合ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "404", description = "集合不存在")
    })
    public void updateCollection(@PathVariable Integer id, 
                               @RequestBody
                               @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                       description = "集合信息", 
                                       required = true,
                                       content = @Content(schema = @Schema(implementation = CollectionEntity.class)))
                               CollectionEntity collection) {
        try {
            System.out.println("CollectionController - 接收到更新请求，路径ID: " + id);
            System.out.println("CollectionController - 请求体: " + collection);
            
            // 设置ID
            collection.setId(id);
            System.out.println("CollectionController - 设置ID后的集合对象: " + collection);
            
            // 调用服务层
            System.out.println("CollectionController - 准备调用service.updateCollection");
            collectionService.updateCollection(collection);
            System.out.println("CollectionController - service.updateCollection调用成功");
        } catch (Exception e) {
            System.out.println("CollectionController - 发生错误: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 删除集合
    @DeleteMapping("/{id}")
    @Operation(summary = "删除集合", description = "删除指定ID的集合")
    @Parameters(value = {
            @Parameter(name = "id", description = "集合ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "集合不存在")
    })
    public void deleteCollection(@PathVariable Integer id) {
        collectionService.deleteCollection(id);
    }
    
    @PostMapping("/merge")
    @Operation(summary = "合并集合", description = "将多个源集合合并到目标集合中，并自动去重，合并后删除源集合")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "合并成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "404", description = "目标集合或源集合不存在")
    })
    public void mergeCollections(@RequestBody 
                               @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                       description = "合并集合参数", 
                                       required = true,
                                       content = @Content(schema = @Schema(implementation = MergeCollectionsRequest.class)))
                               MergeCollectionsRequest mergeRequest) {
        collectionService.mergeCollections(mergeRequest.getTargetCollectionId(), mergeRequest.getSourceCollectionIds());
    }
    
    // AI筛选记录并生成集合
    @PostMapping("/ai-generate")
    @Operation(summary = "AI筛选记录生成集合", description = "根据用户描述，由AI自动筛选符合的记录并生成集合")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "集合生成成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "500", description = "生成集合失败")
    })
    public Map<String, Object> generateCollectionByAIFilter(@RequestBody 
                                                           @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                                   description = "包含用户描述的请求体", 
                                                                   required = true)
                                                           Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. 获取用户描述
            String description = request.get("description");
            if (description == null || description.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "描述不能为空");
                return response;
            }
            
            // 2. 获取所有记录
            List<Record> allRecords = recordService.getAllRecords();
            
            // 3. AI分析描述并筛选记录
            List<Integer> matchingRecordIds = aiService.analyzeDescriptionAndFilterRecords(description, allRecords);
            
            // 4. 生成集合名称
            String collectionName = aiService.generateCollectionName(description);
            
            // 5. 创建新集合
            CollectionEntity newCollection = new CollectionEntity();
            newCollection.setName(collectionName);
            newCollection.setDescription(description);

            collectionService.createCollection(newCollection);
            
            // 6. 将匹配的记录关联到新集合
            for (Integer recordId : matchingRecordIds) {
                Record record = recordService.findById(recordId);
                if (record != null) {
                    RecordCollection recordCollection = new RecordCollection(recordId, newCollection.getId());
                    recordCollectionMapper.addRecordToCollection(recordId, newCollection.getId());
                }
            }
            
            // 7. 构建响应
            response.put("success", true);
            response.put("message", "集合生成成功");
            response.put("collection", newCollection);
            response.put("matchedRecordsCount", matchingRecordIds.size());
            
            return response;
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "生成集合失败: " + e.getMessage());
            return response;
        }
    }
    
    // 根据集合ID获取记录列表
    @GetMapping("/{id}/records")
    @Operation(summary = "根据集合ID获取记录列表", description = "获取指定集合中的所有记录")
    @Parameters(value = {
            @Parameter(name = "id", description = "集合ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "集合不存在")
    })
    public List<Record> getRecordsByCollectionId(@PathVariable Integer id) {
        return collectionService.findRecordsByCollectionId(id);
    }
    
    // 将记录添加到集合
    @PostMapping("/{collectionId}/records/{recordId}")
    @Operation(summary = "将记录添加到集合", description = "将指定的记录添加到指定的集合中")
    @Parameters(value = {
            @Parameter(name = "collectionId", description = "集合ID", required = true),
            @Parameter(name = "recordId", description = "记录ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "添加成功"),
            @ApiResponse(responseCode = "404", description = "集合或记录不存在")
    })
    public void addRecordToCollection(@PathVariable Integer collectionId, @PathVariable Integer recordId) {
        collectionService.addRecordToCollection(recordId, collectionId);
    }
    
    // 将记录从集合中移除
    @DeleteMapping("/{collectionId}/records/{recordId}")
    @Operation(summary = "将记录从集合中移除", description = "将指定的记录从指定的集合中移除")
    @Parameters(value = {
            @Parameter(name = "collectionId", description = "集合ID", required = true),
            @Parameter(name = "recordId", description = "记录ID", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "移除成功"),
            @ApiResponse(responseCode = "404", description = "集合或记录不存在")
    })
    public void removeRecordFromCollection(@PathVariable Integer collectionId, @PathVariable Integer recordId) {
        collectionService.removeRecordFromCollection(recordId, collectionId);
    }
}