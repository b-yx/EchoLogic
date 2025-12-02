package org.example.dao.service;

import org.example.dao.pojo.Record;
import org.example.dao.pojo.Tagx;

import java.util.List;
import java.util.Map;

public interface RecordService {
    // 获取所有记录
    List<Record> findAll();
    
    // 根据ID获取记录
    Record findById(Integer id);
    
    // 根据集合ID获取记录列表
    List<Record> findByCollectionId(Integer collectionId);
    
    // 创建记录
    void createRecord(Record record);
    
    // 更新记录
    void updateRecord(Record record);
    
    // 删除记录
    void deleteRecord(Integer id);
    
    // 为记录添加标签
    void addTagToRecord(Integer recordId, Integer tagId);
    
    // 从记录中移除标签
    void removeTagFromRecord(Integer recordId, Integer tagId);
    
    // 获取记录的所有标签
    List<Tagx> findRecordTags(Integer recordId);
    
    // 搜索记录
    List<Record> searchRecords(String keyword);
    
    // 根据类型查询记录
    List<Record> findByType(String contentType);
    
    // 保存草稿
    void saveDraft(Integer id, Map<String, Object> content);
    
    // 恢复记录
    void restoreRecord(Integer id);
    
    // 生成标签推荐
    List<Tagx> generateTagRecommendations(Integer id);
}