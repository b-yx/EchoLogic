package org.example.dao.service;

import org.example.dao.pojo.UserDailyTask;
import java.util.List;
import org.example.dao.pojo.UserBehavior;

public interface DailyTaskService {
    List<UserDailyTask> getTodayTasks(Long userId);
    void updateTaskProgress(Long userId, UserBehavior.BehaviorType behaviorType);
    boolean receiveReward(Long userId, Long taskId);
}
