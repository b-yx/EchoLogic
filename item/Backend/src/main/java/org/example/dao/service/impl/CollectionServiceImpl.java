package org.example.dao.service.impl;

import org.example.dao.mapper.CollectionMapper;
import org.example.dao.mapper.RecordMapper;
import org.example.dao.mapper.RecordCollectionMapper;
import org.example.dao.pojo.CollectionEntity;
import org.example.dao.pojo.Record;
import org.example.dao.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CollectionServiceImpl implements CollectionService {
    private static final Logger logger = LoggerFactory.getLogger(CollectionServiceImpl.class);

    @Autowired
    private CollectionMapper collectionMapper;
    
    @Autowired
    private RecordMapper recordMapper;
    
    @Autowired
    private RecordCollectionMapper recordCollectionMapper;



    @Override
    public List<CollectionEntity> findAll() {
        return collectionMapper.findAll();
    }

    @Override
    public CollectionEntity findById(Integer id) {
        return collectionMapper.findById(id);
    }

    @Override
    public void createCollection(CollectionEntity collection) {
        Date now = new Date();
        collection.setCreateTime(now);
        collection.setUpdateTime(now);
        collectionMapper.insert(collection);
    }

    @Override
    @Transactional
    public void updateCollection(CollectionEntity collection) {
        // 添加更详细的调试信息
        System.out.println("=== 开始执行updateCollection ===");
        System.out.println("collection对象是否为null: " + (collection == null));
        
        if (collection != null && collection.getId() != null) {
            // 打印传入的完整对象信息
            System.out.println("collection对象完整信息: " + collection.toString());
            System.out.println("collection.getId(): " + collection.getId());
            System.out.println("collection.getName(): " + collection.getName());
            System.out.println("collection.getDescription(): " + collection.getDescription());
            
            try {
                // 直接设置更新时间
                collection.setUpdateTime(new Date());
                System.out.println("设置更新时间完成: " + collection.getUpdateTime());
                
                // 确保description不为null
                if (collection.getDescription() == null) {
                    collection.setDescription("");
                    System.out.println("设置了空描述");
                }
                
                // 不覆盖已有名称，只有当名称为空时才设置默认值
                if (collection.getName() != null && !collection.getName().trim().isEmpty()) {
                    System.out.println("使用传入的名称: " + collection.getName());
                } else {
                    collection.setName("未命名集合");
                    System.out.println("设置了默认名称");
                }
                
                // 验证mapper是否注入成功
                System.out.println("collectionMapper是否为null: " + (collectionMapper == null));
                
                // 先检查记录是否存在，使用新的findByIdWithCast方法
                System.out.println("检查记录是否存在: id=" + collection.getId());
                CollectionEntity existing = collectionMapper.findByIdWithCast(collection.getId());
                System.out.println("查询结果: " + (existing != null ? existing.toString() : "null"));
                
                if (existing != null) {
                    // 打印准备更新的数据
                    System.out.println("准备更新的数据: id=" + collection.getId() + ", name=" + 
                                      collection.getName() + ", description=" + collection.getDescription());
                    
                    // 使用单独的SQL更新语句，不依赖对象映射
                    System.out.println("准备直接执行UPDATE SQL语句");
                    int updateResult = collectionMapper.update(collection);
                    System.out.println("update执行完成，影响行数: " + updateResult);
                    
                    // 强制刷新事务
                    System.out.println("强制刷新事务");
                    
                    // 强制查询数据库，使用原生SQL
                    System.out.println("强制查询数据库验证更新结果");
                    CollectionEntity updated = collectionMapper.findByIdWithCast(collection.getId());
                    System.out.println("更新后的数据: " + (updated != null ? updated.toString() : "null"));
                    if (updated != null) {
                        System.out.println("更新后的name: " + updated.getName());
                        System.out.println("更新后的description: " + updated.getDescription());
                        System.out.println("更新后的updateTime: " + updated.getUpdateTime());
                    }
                } else {
                    System.out.println("警告: 未找到ID为" + collection.getId() + "的集合，跳过更新");
                }
                
            } catch (Exception e) {
                System.out.println("=== 捕获到异常 ===");
                System.out.println("异常类型: " + e.getClass().getName());
                System.out.println("异常消息: " + e.getMessage());
                System.out.println("=== 异常堆栈 ===");
                e.printStackTrace(System.out);
                throw e;
            }
        }
        System.out.println("=== updateCollection执行完成 ===");
    }

    @Override
    public void deleteCollection(Integer id) {
        // 先删除关联记录
        recordCollectionMapper.clearCollectionRecords(id);
        // 再删除集合
        collectionMapper.delete(id);
    }
    
    @Override
    public List<Record> findRecordsByCollectionId(Integer collectionId) {
        List<Integer> recordIds = recordCollectionMapper.findRecordsByCollectionId(collectionId);
        List<Record> records = new ArrayList<>();
        for (Integer recordId : recordIds) {
            Record record = recordMapper.findById(recordId);
            if (record != null) {
                records.add(record);
            }
        }
        return records;
    }
    
    @Override
    public void addRecordToCollection(Integer recordId, Integer collectionId) {
        recordCollectionMapper.addRecordToCollection(recordId, collectionId);
        Long userId = 1L; // 暂时硬编码为 1L，需要根据实际情况修改
    }
    
    @Override
    public void removeRecordFromCollection(Integer recordId, Integer collectionId) {
        recordCollectionMapper.removeRecordFromCollection(recordId, collectionId);
    }
    
    @Override
    @Transactional
    public void mergeCollections(Integer targetCollectionId, List<Integer> sourceCollectionIds, String name, String description) {
        // 获取目标集合已有的记录ID
        List<Integer> existingRecordIds = recordCollectionMapper.findRecordsByCollectionId(targetCollectionId);
        
        // 遍历所有源集合
        for (Integer sourceCollectionId : sourceCollectionIds) {
            // 获取源集合中的记录ID
            List<Integer> sourceRecordIds = recordCollectionMapper.findRecordsByCollectionId(sourceCollectionId);
            
            // 将源集合中的记录添加到目标集合中（自动去重）
            for (Integer recordId : sourceRecordIds) {
                if (!existingRecordIds.contains(recordId)) {
                    recordCollectionMapper.addRecordToCollection(recordId, targetCollectionId);
                    existingRecordIds.add(recordId); // 更新已存在记录列表
                }
            }
            
            // 合并完成后删除源集合
            deleteCollection(sourceCollectionId);
        }
        
        // 更新目标集合的信息
        CollectionEntity targetCollection = findById(targetCollectionId);
        if (targetCollection != null) {
            // 更新名称（如果提供了新名称）
            if (name != null && !name.trim().isEmpty()) {
                targetCollection.setName(name.trim());
            }
            
            // 更新描述（如果提供了新描述）
            if (description != null) {
                targetCollection.setDescription(description);
            }
            
            // 更新更新时间
            targetCollection.setUpdateTime(new Date());
            
            // 保存更新后的集合
            collectionMapper.update(targetCollection);
        }
    }
}
