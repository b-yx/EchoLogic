package org.example.dao.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionEntity {
    private Integer id;
    private String name;  // 集合名称
    private String description;  // 集合描述（可选）
    private Date createTime;  // 创建时间
    private Date updateTime;  // 更新时间
}