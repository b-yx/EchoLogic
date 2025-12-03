package org.example.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.dao.mapper.DailyTaskMapper;
import org.example.dao.mapper.UserDailyTaskMapper;
import org.example.dao.pojo.DailyTask;
import org.example.dao.pojo.UserBehavior;
import org.example.dao.pojo.UserDailyTask;
import org.example.dao.service.DailyTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DailyTaskServiceImpl implements DailyTaskService {

    @Autowired
    private DailyTaskMapper dailyTaskMapper;

    @Autowired
    private UserDailyTaskMapper userDailyTaskMapper;

    @Override
    public List<UserDailyTask> getTodayTasks(Long userId) {
        // 简单实现：假设每日任务在用户首次查询时初始化
        QueryWrapper<UserDailyTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        // 实际项目中，需要加入日期判断，这里简化
        if (userDailyTaskMapper.selectCount(queryWrapper) == 0) {
            List<DailyTask> allTasks = dailyTaskMapper.selectList(null);
            List<UserDailyTask> userTasks = allTasks.stream().map(task -> {
                UserDailyTask userTask = new UserDailyTask();
                userTask.setUserId(userId);
                userTask.setTaskId(task.getId());
                userTask.setCurrentCount(0);
                userTask.setIsCompleted(false);
                userTask.setRewardReceived(false);
                userDailyTaskMapper.insert(userTask);
                return userTask;
            }).collect(Collectors.toList());
            return userTasks;
        }
        return userDailyTaskMapper.selectList(queryWrapper);
    }

    @Override
    public void updateTaskProgress(Long userId, UserBehavior.BehaviorType behaviorType) {
        // 根据行为类型找到对应的任务并更新
        // 此处简化逻辑，实际应根据 behaviorType 匹配具体任务
        // 例如：COLLECT -> "今日收藏3条内容"
    }

    @Override
    public boolean receiveReward(Long userId, Long taskId) {
        QueryWrapper<UserDailyTask> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("task_id", taskId);
        UserDailyTask userTask = userDailyTaskMapper.selectOne(qw);

        if (userTask != null && userTask.getIsCompleted() && !userTask.getRewardReceived()) {
            userTask.setRewardReceived(true);
            userDailyTaskMapper.updateById(userTask);
            // 此处应调用用户服务，为用户增加AI次数等奖励
            return true;
        }
        return false;
    }
}
