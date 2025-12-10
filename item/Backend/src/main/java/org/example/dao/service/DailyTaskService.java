package org.example.dao.service;

import org.example.dao.pojo.UserDailyTask;
import java.util.List;

public interface DailyTaskService {
    List<UserDailyTask> getTodayTasks(Long userId);
    boolean receiveReward(Long userId, Long taskId);
}
