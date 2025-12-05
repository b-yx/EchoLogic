package org.example.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_ai_quota")
public class UserQuota {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer quota;
}
