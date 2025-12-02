package org.example.dao.service;

import org.example.dao.pojo.Tagx;

import java.util.List;

public interface TagxService {
    // 获取所有标签
    List<Tagx> findAll();
    
    // 根据ID获取标签
    Tagx findById(Integer id);
    
    // 创建标签
    void createTag(Tagx tag);
    
    // 更新标签
    void updateTag(Tagx tag);
    
    // 删除标签
    void deleteTag(Integer id);
    
    // 根据记录ID获取标签列表
    List<Tagx> findTagsByRecordId(Integer recordId);
}