package org.example.dao.service;

import org.example.dao.pojo.UserAchievement;
import org.example.dao.pojo.UserBehavior;
import java.util.List;

public interface UserAchievementService {
    void updateAchievementProgress(Long userId, UserBehavior.BehaviorType behaviorType);
    List<UserAchievement> getUserAchievements(Long userId);
}
