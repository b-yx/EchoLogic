package org.example.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_behavior")
public class UserBehavior {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BehaviorType behaviorType;
    private LocalDateTime behaviorTime;
    private Long relatedId;
    private LocalDateTime createTime;

    public enum BehaviorType {
        COLLECT,
        INCUBATE,
        VIEW,
        EDIT,
        GATHER
    }
}
