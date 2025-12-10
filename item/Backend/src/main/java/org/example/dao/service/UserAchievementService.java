package org.example.dao.service;

import org.example.dao.pojo.UserAchievement;
import java.util.List;

public interface UserAchievementService {
    // 移除了对UserBehavior的依赖
    List<UserAchievement> getUserAchievements(Long userId);
}
