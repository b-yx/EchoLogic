package org.example.dao.mapper;

import org.apache.ibatis.annotations.*;
import org.example.dao.pojo.CollectionEntity;

import java.util.List;

@Mapper
public interface CollectionMapper {
    // 查询所有集合
    @Select("SELECT * FROM collection ORDER BY update_time DESC")
    List<CollectionEntity> findAll();
    
    // 根据ID查询集合
    @Select("SELECT * FROM collection WHERE id = #{id}")
    CollectionEntity findById(Integer id);
    
    // 添加集合
    @Insert("INSERT INTO collection(name, description, create_time, update_time) VALUES(#{name}, #{description}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CollectionEntity collection);
    
    // 更新集合
    @Update("UPDATE collection SET name = #{name}, description = #{description}, update_time = #{updateTime} WHERE id = #{id}")
    int update(CollectionEntity collection);
    
    // 查询集合详情（用于调试）
    @Select("SELECT id, name, CAST(description AS CHAR) as description, create_time, update_time FROM collection WHERE id = #{id}")
    CollectionEntity findByIdWithCast(Integer id);
    
    // 删除集合
    @Delete("DELETE FROM collection WHERE id = #{id}")
    void delete(Integer id);
}