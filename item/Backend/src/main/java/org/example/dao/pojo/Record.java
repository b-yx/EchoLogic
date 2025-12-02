package org.example.dao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Record {
    private Integer id;
    private String title;  // 记录标题
    private String content;  // 记录的文本内容
    private String contentType;  // 记录类型
    private Integer collectionId;  // 所属的集合ID
    private Date createTime;  // 创建时间
    private Date updateTime;  // 更新时间
    private Boolean deleted = false;  // 是否删除（用于软删除）
    private List<Tagx> tags;  // 关联的标签列表
}