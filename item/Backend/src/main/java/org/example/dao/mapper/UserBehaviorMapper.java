package org.example.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.dao.pojo.UserBehavior;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {

    /**
     * 按天聚合统计用户行为
     */
    @Select("SELECT DATE(behavior_time) as date, behavior_type as type, COUNT(*) as count " +
            "FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE(behavior_time), behavior_type " +
            "ORDER BY date")
    List<Map<String, Object>> countByDateRangeGroupedByDay(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Select("SELECT behavior_type as type, COUNT(*) as count FROM user_behavior WHERE related_id = #{relatedId} GROUP BY behavior_type")
    List<Map<String, Object>> countByRelatedId(@Param("relatedId") Long relatedId);

    @Select("SELECT behavior_type as type, COUNT(*) as count FROM user_behavior WHERE related_id = #{relatedId} AND user_id = #{userId} GROUP BY behavior_type")
    List<Map<String, Object>> countByRelatedIdAndUser(@Param("relatedId") Long relatedId, @Param("userId") Long userId);
}
