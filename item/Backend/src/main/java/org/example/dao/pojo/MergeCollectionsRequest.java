package org.example.dao.pojo;

import lombok.Data;

import java.util.List;

/**
 * 合并集合请求参数类
 */
@Data
public class MergeCollectionsRequest {
    /**
     * 目标集合ID
     */
    private Integer targetCollectionId;
    
    /**
     * 源集合ID列表
     */
    private List<Integer> sourceCollectionIds;
}
