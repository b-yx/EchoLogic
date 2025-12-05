package org.example.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.dao.mapper.UserAchievementMapper;
import org.example.dao.mapper.UserBehaviorMapper;
import org.example.dao.pojo.UserAchievement;
import org.example.dao.pojo.UserBehavior;
import org.example.dao.service.UserAchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAchievementServiceImpl implements UserAchievementService {

    @Autowired
    private UserAchievementMapper userAchievementMapper;

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Override
    public void updateAchievementProgress(Long userId, UserBehavior.BehaviorType behaviorType) {
        // 1. 找出与该行为类型相关且用户尚未解锁的成就
        QueryWrapper<UserAchievement> achievementQw = new QueryWrapper<>();
        achievementQw.eq("user_id", userId)
                     .eq("behavior_type", behaviorType.name())
                     .eq("is_unlocked", false);
        List<UserAchievement> achievements = userAchievementMapper.selectList(achievementQw);

        if (achievements.isEmpty()) {
            String keyword = behaviorType == UserBehavior.BehaviorType.COLLECT ? "收藏" : "孵化";
            QueryWrapper<UserAchievement> fallbackQw = new QueryWrapper<>();
            fallbackQw.eq("user_id", userId)
                      .isNull("behavior_type")
                      .like("achievement_name", keyword)
                      .eq("is_unlocked", false);
            achievements = userAchievementMapper.selectList(fallbackQw);
            if (achievements.isEmpty()) return;
        }

        for (UserAchievement achievement : achievements) {
            if (achievement.getBehaviorType() == null) {
                achievement.setBehaviorType(behaviorType);
            }
            achievement.setCurrentValue(achievement.getCurrentValue() + 1);
            if (achievement.getCurrentValue() >= achievement.getConditionValue()) {
                achievement.setIsUnlocked(true);
                achievement.setUnlockTime(LocalDateTime.now());
            }
            userAchievementMapper.updateById(achievement);
        }
    }

    @Override
    public List<UserAchievement> getUserAchievements(Long userId) {
        QueryWrapper<UserAchievement> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        return userAchievementMapper.selectList(qw);
    }
}
