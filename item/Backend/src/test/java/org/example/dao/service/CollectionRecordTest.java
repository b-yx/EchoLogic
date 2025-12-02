package org.example.dao.service;

import org.example.dao.pojo.CollectionEntity;
import org.example.dao.pojo.Record;
import org.example.dao.pojo.Tagx;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class CollectionRecordTest {

    @Autowired
    private CollectionService collectionService;
    
    @Autowired
    private RecordService recordService;
    
    @Autowired
    private TagxService tagService;

    @Test
    public void testBasicCollectionOperations() {
        System.out.println("=== 测试集合基本操作 ===");
        
        // 创建测试集合
        CollectionEntity testCollection = new CollectionEntity();
        testCollection.setName("测试集合");
        testCollection.setDescription("这是一个测试集合");
        collectionService.createCollection(testCollection);
        System.out.println("创建集合成功: " + testCollection.getId());
        
        // 获取所有集合
        List<CollectionEntity> collections = collectionService.findAll();
        System.out.println("所有集合数量: " + collections.size());
        collections.forEach(c -> System.out.println("集合: " + c.getName()));
    }

    @Test
    public void testRecordOperations() {
        System.out.println("=== 测试记录操作 ===");
        
        // 先创建一个集合用于测试
        CollectionEntity collection = new CollectionEntity();
        collection.setName("记录测试集合");
        collectionService.createCollection(collection);
        
        // 创建带有时间戳的唯一标签，避免重复插入
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        // 创建标签
        Tagx tag1 = new Tagx();
        tag1.setName("测试标签1-" + timestamp);
        tag1.setColor("#FF5733");
        tagService.createTag(tag1);
        
        Tagx tag2 = new Tagx();
        tag2.setName("测试标签2-" + timestamp);
        tag2.setColor("#33FF57");
        tagService.createTag(tag2);
        
        // 创建带标签的记录
        Record record = new Record();
        record.setTitle("测试记录标题");
        record.setContent("这是一条测试记录");
        record.setContentType("text/plain");
        record.setCollectionId(collection.getId());
        record.setCreateTime(new java.util.Date());
        record.setUpdateTime(new java.util.Date());
        record.setTags(Arrays.asList(tag1, tag2));
        recordService.createRecord(record);
        
        System.out.println("创建记录成功: " + record.getId());
        
        // 查询集合中的记录
        List<Record> records = recordService.findByCollectionId(collection.getId());
        System.out.println("集合中的记录数量: " + records.size());
        
        // 查询记录的标签
        List<Tagx> recordTags = recordService.findRecordTags(record.getId());
        System.out.println("记录的标签数量: " + recordTags.size());
        recordTags.forEach(t -> System.out.println("标签: " + t.getName()));
    }
}