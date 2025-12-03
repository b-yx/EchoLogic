package org.example.dao.service.impl;

import org.example.dao.mapper.UserBehaviorMapper;
import org.example.dao.pojo.UserBehavior;
import org.example.dao.service.DailyTaskService;
import org.example.dao.service.UserAchievementService;
import org.example.dao.service.UserBehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class UserBehaviorServiceImpl implements UserBehaviorService {

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Autowired
    private DailyTaskService dailyTaskService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Override
    @Transactional
    public void recordBehavior(Long userId, UserBehavior.BehaviorType type, Long relatedId) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setBehaviorType(type);
        behavior.setRelatedId(relatedId);
        behavior.setBehaviorTime(LocalDateTime.now());
        behavior.setCreateTime(LocalDateTime.now());
        userBehaviorMapper.insert(behavior);

        // --- 关键逻辑：关联更新任务和成就 ---
        dailyTaskService.updateTaskProgress(userId, type);
        userAchievementService.updateAchievementProgress(userId, type);
    }

    @Override
    public List<Map<String, Object>> getBehaviorStats(Long userId, String timeRange) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime;
        if ("year".equalsIgnoreCase(timeRange)) {
            startTime = endTime.minus(1, ChronoUnit.YEARS);
        } else { // 默认近30天
            startTime = endTime.minus(30, ChronoUnit.DAYS);
        }
        return userBehaviorMapper.countByDateRangeGroupedByDay(userId, startTime, endTime);
    }
}
