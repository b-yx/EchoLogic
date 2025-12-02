package org.example.dao.mapper;

import org.apache.ibatis.annotations.*;
import org.example.dao.pojo.Tagx;

import java.util.List;

@Mapper
public interface TagxMapper {
    // 查询所有标签
    @Select("SELECT * FROM tag")
    List<Tagx> findAll();
    
    // 根据ID查询标签
    @Select("SELECT * FROM tag WHERE id = #{id}")
    Tagx findById(Integer id);
    
    // 添加标签
    @Insert("INSERT INTO tag(name, color) VALUES(#{name}, #{color})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Tagx tag);
    
    // 更新标签
    @Update("UPDATE tag SET name = #{name}, color = #{color} WHERE id = #{id}")
    void update(Tagx tag);
    
    // 删除标签
    @Delete("DELETE FROM tag WHERE id = #{id}")
    void delete(Integer id);
    
    // 根据记录ID查询关联的标签
    @Select("SELECT t.* FROM tag t JOIN record_tag rt ON t.id = rt.tag_id WHERE rt.record_id = #{recordId}")
    List<Tagx> findByRecordId(Integer recordId);
}