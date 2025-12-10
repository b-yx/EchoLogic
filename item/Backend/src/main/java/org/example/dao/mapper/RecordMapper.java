package org.example.dao.mapper;

import org.apache.ibatis.annotations.*;
import org.example.dao.pojo.Record;

import java.util.List;

@Mapper
public interface RecordMapper {
    // 查询所有记录（排除已删除的），并加载关联的标签
    @Select("SELECT * FROM record WHERE deleted = false ORDER BY create_time DESC")
    @Results({
        @Result(column = "id", property = "id"),
        @Result(column = "id", property = "tags", many = @Many(select = "org.example.dao.mapper.TagxMapper.findByRecordId"))
    })
    List<Record> findAll();
    
    // 根据ID查询记录，并加载关联的标签
    @Select("SELECT * FROM record WHERE id = #{id}")
    @Results({
        @Result(column = "id", property = "id"),
        @Result(column = "id", property = "tags", many = @Many(select = "org.example.dao.mapper.TagxMapper.findByRecordId"))
    })
    Record findById(Integer id);
    
    // 添加记录
    @Insert("INSERT INTO record(title, content, content_type, create_time, update_time, deleted) VALUES(#{title}, #{content}, #{contentType}, #{createTime}, #{updateTime}, #{deleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Record record);
    
    // 更新记录
    @Update("UPDATE record SET title = #{title}, content = #{content}, content_type = #{contentType}, update_time = #{updateTime}, deleted = #{deleted} WHERE id = #{id}")
    int update(Record record);
    
    // 删除记录（软删除）
    @Update("UPDATE record SET deleted = true WHERE id = #{id}")
    void delete(Integer id);
    
    // 恢复记录
    @Update("UPDATE record SET deleted = false WHERE id = #{id}")
    void restoreRecord(Integer id);
    
    // 搜索记录
    @Select("SELECT * FROM record WHERE deleted = false AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')) ORDER BY create_time DESC")
    List<Record> searchRecords(String keyword);
    
    // 根据类型查询记录
    @Select("SELECT * FROM record WHERE deleted = false AND content_type = #{contentType} ORDER BY create_time DESC")
    List<Record> findByType(String contentType);
    
    // 为记录添加标签（多对多关系）
    @Insert("INSERT INTO record_tag(record_id, tag_id) VALUES(#{recordId}, #{tagId})")
    void addTagToRecord(@Param("recordId") Integer recordId, @Param("tagId") Integer tagId);
    
    // 移除记录的标签
    @Delete("DELETE FROM record_tag WHERE record_id = #{recordId} AND tag_id = #{tagId}")
    void removeTagFromRecord(@Param("recordId") Integer recordId, @Param("tagId") Integer tagId);
    
    // 清空记录的所有标签
    @Delete("DELETE FROM record_tag WHERE record_id = #{recordId}")
    void clearRecordTags(Integer recordId);
}