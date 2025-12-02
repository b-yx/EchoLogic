package org.example.dao.service;

import org.example.dao.pojo.CollectionEntity;

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
}