package org.example.dao.mapper;

import org.apache.ibatis.annotations.*;
import org.example.dao.pojo.RecordCollection;

import java.util.List;

@Mapper
public interface RecordCollectionMapper {
    // 为记录添加集合关联
    @Insert("INSERT INTO record_collection(record_id, collection_id) VALUES(#{recordId}, #{collectionId})")
    void addRecordToCollection(@Param("recordId") Integer recordId, @Param("collectionId") Integer collectionId);
    
    // 从记录中移除集合关联
    @Delete("DELETE FROM record_collection WHERE record_id = #{recordId} AND collection_id = #{collectionId}")
    void removeRecordFromCollection(@Param("recordId") Integer recordId, @Param("collectionId") Integer collectionId);
    
    // 获取记录关联的所有集合ID
    @Select("SELECT collection_id FROM record_collection WHERE record_id = #{recordId}")
    List<Integer> findCollectionsByRecordId(Integer recordId);
    
    // 获取集合关联的所有记录ID
    @Select("SELECT record_id FROM record_collection WHERE collection_id = #{collectionId}")
    List<Integer> findRecordsByCollectionId(Integer collectionId);
    
    // 检查记录是否已关联到集合
    @Select("SELECT COUNT(*) FROM record_collection WHERE record_id = #{recordId} AND collection_id = #{collectionId}")
    int checkRecordInCollection(@Param("recordId") Integer recordId, @Param("collectionId") Integer collectionId);
    
    // 清空记录的所有集合关联
    @Delete("DELETE FROM record_collection WHERE record_id = #{recordId}")
    void clearRecordCollections(Integer recordId);
    
    // 清空集合的所有记录关联
    @Delete("DELETE FROM record_collection WHERE collection_id = #{collectionId}")
    void clearCollectionRecords(Integer collectionId);
}
