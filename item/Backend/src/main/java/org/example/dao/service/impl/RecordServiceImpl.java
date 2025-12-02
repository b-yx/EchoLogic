package org.example.dao.service.impl;

import org.example.dao.mapper.RecordMapper;
import org.example.dao.mapper.TagxMapper;
import org.example.dao.pojo.Record;
import org.example.dao.pojo.Tagx;
import org.example.dao.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class RecordServiceImpl implements RecordService {

    @Autowired
    private RecordMapper recordMapper;
    
    @Autowired
    private TagxMapper tagMapper;

    @Override
    public List<Record> findAll() {
        return recordMapper.findAll();
    }

    @Override
    public Record findById(Integer id) {
        return recordMapper.findById(id);
    }

    @Override
    public List<Record> findByCollectionId(Integer collectionId) {
        return recordMapper.findByCollectionId(collectionId);
    }

    @Transactional
    @Override
    public void createRecord(Record record) {
        // 验证必填字段
        if (record.getTitle() == null || record.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (record.getContent() == null || record.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("内容不能为空");
        }
        // 如果记录类型为空，设置默认值为"text"
        if (record.getContentType() == null || record.getContentType().trim().isEmpty()) {
            System.out.println("记录类型为空，设置默认值为'text'");
            record.setContentType("text");
        }
        // 如果所属集合ID为空，设置默认值为2（数据库中存在的集合ID）
        if (record.getCollectionId() == null) {
            System.out.println("所属集合ID为空，设置默认值为2");
            record.setCollectionId(2);
        }
        
        // 设置创建时间和更新时间
        Date now = new Date();
        record.setCreateTime(now);
        record.setUpdateTime(now);
        
        // 确保deleted字段有值
        if (record.getDeleted() == null) {
            record.setDeleted(false);
        }
        
        // 插入记录
        recordMapper.insert(record);
        
        // 如果有标签，建立关联关系
        if (record.getTags() != null && !record.getTags().isEmpty()) {
            for (Tagx tag : record.getTags()) {
                if (tag != null && tag.getId() != null) {
                    recordMapper.addTagToRecord(record.getId(), tag.getId());
                }
            }
        }
    }

    @Transactional
    @Override
    public void updateRecord(Record record) {
        // 验证参数
        System.out.println("=== 开始执行updateRecord ===");
        System.out.println("record对象是否为null: " + (record != null));
        
        if (record == null) {
            throw new IllegalArgumentException("记录对象不能为空");
        }
        
        if (record.getId() == null) {
            throw new IllegalArgumentException("记录ID不能为空");
        }
        
        // 检查记录是否存在
        System.out.println("检查记录是否存在: id=" + record.getId());
        Record existingRecord = recordMapper.findById(record.getId());
        if (existingRecord == null) {
            throw new RuntimeException("记录不存在，ID: " + record.getId());
        }
        
        // 设置更新时间
        Date now = new Date();
        record.setUpdateTime(now);
        System.out.println("设置更新时间完成: " + now);
        
        // 验证必填字段
        if (record.getTitle() == null || record.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        
        if (record.getContent() == null) {
            record.setContent("");
        }
        
        // 设置contentType默认值，避免为空
        if (record.getContentType() == null || record.getContentType().trim().isEmpty()) {
            record.setContentType("text");
            System.out.println("contentType为空，设置默认值为text");
        }
        
        // 执行更新
        System.out.println("准备执行更新，记录ID: " + record.getId());
        int affectedRows = recordMapper.update(record);
        System.out.println("update执行完成，影响行数: " + affectedRows);
        
        // 验证更新结果
        if (affectedRows == 0) {
            throw new RuntimeException("更新失败，记录不存在或未更新任何字段");
        }
        
        System.out.println("=== updateRecord执行完成 ===");
    }

    @Transactional
    @Override
    public void deleteRecord(Integer id) {
        // 先删除关联的标签关系
        recordMapper.clearRecordTags(id);
        // 再删除记录
        recordMapper.delete(id);
    }

    @Override
    public void addTagToRecord(Integer recordId, Integer tagId) {
        recordMapper.addTagToRecord(recordId, tagId);
    }

    @Override
    public void removeTagFromRecord(Integer recordId, Integer tagId) {
        recordMapper.removeTagFromRecord(recordId, tagId);
    }

    @Override
    public List<Tagx> findRecordTags(Integer recordId) {
        return tagMapper.findByRecordId(recordId);
    }

    @Override
    public List<Record> searchRecords(String keyword) {
        return recordMapper.searchRecords(keyword);
    }

    @Override
    public List<Record> findByType(String contentType) {
        return recordMapper.findByType(contentType);
    }

    @Override
    public void saveDraft(Integer id, Map<String, Object> content) {
        // 实现草稿保存逻辑
        Record record = recordMapper.findById(id);
        if (record != null) {
            // 根据content中的字段更新记录
            if (content.containsKey("title")) {
                record.setTitle(content.get("title").toString());
            }
            if (content.containsKey("content")) {
                record.setContent(content.get("content").toString());
            }
            if (content.containsKey("contentType")) {
                record.setContentType(content.get("contentType").toString());
            }
            record.setUpdateTime(new Date());
            recordMapper.update(record);
        }
    }

    @Override
    public void restoreRecord(Integer id) {
        // 实现记录恢复逻辑
        recordMapper.restoreRecord(id);
    }

    @Override
    public List<Tagx> generateTagRecommendations(Integer id) {
        // 实现标签推荐逻辑
        Record record = recordMapper.findById(id);
        if (record != null) {
            // 这里可以实现更复杂的标签推荐算法
            // 暂时返回所有标签供前端选择
            return tagMapper.findAll();
        }
        return null;
    }
}