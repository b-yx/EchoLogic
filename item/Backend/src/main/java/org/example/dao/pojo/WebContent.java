package org.example.dao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebContent {
    private Integer id;
    private Integer collectionId; // 关联的集合ID
    private String sourceUrl; // 来源链接
    private String title; // 标题
    private String parsedText; // 正文纯文本快照
    private String coverImagePath; // 封面图本地路径
    private String summary; // AI 摘要
    private String tags; // AI 标签
    private String mediaType; // web, audio, video
    private Date createTime; // 创建时间
}