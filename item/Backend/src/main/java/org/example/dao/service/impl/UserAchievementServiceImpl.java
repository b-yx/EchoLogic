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
        // 1. 找出该用户所有未解锁的成就
        QueryWrapper<UserAchievement> achievementQw = new QueryWrapper<>();
        achievementQw.eq("user_id", userId).eq("is_unlocked", false);
        List<UserAchievement> achievements = userAchievementMapper.selectList(achievementQw);

        if (achievements.isEmpty()) return;

        // 2. 根据行为类型，更新相关成就进度
        for (UserAchievement achievement : achievements) {
            // 简单示例：如果成就是关于收藏的
            if (achievement.getAchievementName().contains("收藏") && behaviorType == UserBehavior.BehaviorType.COLLECT) {
                // 统计总收藏数
                QueryWrapper<UserBehavior> behaviorQw = new QueryWrapper<>();
                behaviorQw.eq("user_id", userId).eq("behavior_type", UserBehavior.BehaviorType.COLLECT);
                Long totalCollects = userBehaviorMapper.selectCount(behaviorQw);

                achievement.setCurrentValue(totalCollects.intValue());

                // 3. 判断是否解锁
                if (achievement.getCurrentValue() >= achievement.getConditionValue()) {
                    achievement.setIsUnlocked(true);
                    achievement.setUnlockTime(LocalDateTime.now());
                }
                userAchievementMapper.updateById(achievement);
            }
            // ...可以扩展其他成就类型的判断逻辑
        }
    }

    @Override
    public List<UserAchievement> getUserAchievements(Long userId) {
        QueryWrapper<UserAchievement> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        return userAchievementMapper.selectList(qw);
    }
}
