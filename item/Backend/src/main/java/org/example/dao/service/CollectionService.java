package org.example.dao.service;

import org.example.dao.pojo.CollectionEntity;
import org.example.dao.pojo.Record;

import java.util.List;

public interface CollectionService {
    // 获取所有集合
    List<CollectionEntity> findAll();
    
    // 根据ID获取集合
    CollectionEntity findById(Integer id);
    
    // 创建集合
    void createCollection(CollectionEntity collection);
    
    // 更新集合
    void updateCollection(CollectionEntity collection);
    
    // 删除集合
    void deleteCollection(Integer id);
    
    // 根据集合ID获取记录列表
    List<Record> findRecordsByCollectionId(Integer collectionId);
    
    // 将记录添加到集合
    void addRecordToCollection(Integer recordId, Integer collectionId);
    
    // 将记录从集合移除
    void removeRecordFromCollection(Integer recordId, Integer collectionId);
    
    // 合并集合
    void mergeCollections(Integer targetCollectionId, List<Integer> sourceCollectionIds, String name, String description);
}