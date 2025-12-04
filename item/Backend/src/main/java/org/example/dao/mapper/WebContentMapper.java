package org.example.dao.mapper;

import org.apache.ibatis.annotations.*;
import org.example.dao.pojo.WebContent;

@Mapper
public interface WebContentMapper {
    // 添加网页内容
    @Insert("INSERT INTO web_content(collection_id, source_url, title, parsed_text, cover_image_path, summary, tags, media_type, create_time) " +
            "VALUES(#{collectionId}, #{sourceUrl}, #{title}, #{parsedText}, #{coverImagePath}, #{summary}, #{tags}, #{mediaType}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WebContent webContent);
    
    // 根据集合ID查询网页内容
    @Select("SELECT * FROM web_content WHERE collection_id = #{collectionId}")
    WebContent findByCollectionId(Integer collectionId);
    
    // 根据ID查询网页内容
    @Select("SELECT * FROM web_content WHERE id = #{id}")
    WebContent findById(Integer id);
    
    // 更新网页内容
    @Update("UPDATE web_content SET source_url = #{sourceUrl}, title = #{title}, parsed_text = #{parsedText}, " +
            "cover_image_path = #{coverImagePath}, summary = #{summary}, tags = #{tags}, media_type = #{mediaType} " +
            "WHERE id = #{id}")
    int update(WebContent webContent);
    
    // 删除网页内容
    @Delete("DELETE FROM web_content WHERE id = #{id}")
    void delete(Integer id);
    
    // 根据集合ID删除网页内容
    @Delete("DELETE FROM web_content WHERE collection_id = #{collectionId}")
    void deleteByCollectionId(Integer collectionId);
}