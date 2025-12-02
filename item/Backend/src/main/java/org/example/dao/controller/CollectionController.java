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
import org.example.dao.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
@Tag(name = "集合管理", description = "集合的增删改查操作")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

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
            @ApiResponse(responseCode = "204", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "集合不存在")
    })
    public void deleteCollection(@PathVariable Integer id) {
        collectionService.deleteCollection(id);
    }
}