package org.example.dao.service;

import org.example.dao.pojo.UserBehavior;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface UserBehaviorService {
    void recordBehavior(Long userId, UserBehavior.BehaviorType type, Long relatedId);
    List<Map<String, Object>> getBehaviorStats(Long userId, String timeRange);
    Map<String, Long> getItemBehaviorCounts(Long relatedId, Long userId);
}
