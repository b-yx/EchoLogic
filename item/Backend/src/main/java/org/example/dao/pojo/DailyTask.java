package org.example.dao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("daily_task")
public class DailyTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskName;
    private String behaviorType;
    private Integer targetCount;
    private Integer reward; // 奖励的AI使用次数
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
