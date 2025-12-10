package org.example.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.dao.mapper.DailyTaskMapper;
import org.example.dao.mapper.UserDailyTaskMapper;
import org.example.dao.pojo.DailyTask;
import org.example.dao.pojo.UserDailyTask;
import org.example.dao.service.DailyTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.List;


@Service
public class DailyTaskServiceImpl implements DailyTaskService {

    @Autowired
    private DailyTaskMapper dailyTaskMapper;

    @Autowired
    private UserDailyTaskMapper userDailyTaskMapper;



    @Override
    public List<UserDailyTask> getTodayTasks(Long userId) {
        LocalDate today = LocalDate.now();
        QueryWrapper<UserDailyTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("create_date", today);
        List<UserDailyTask> todayTasks = userDailyTaskMapper.selectList(queryWrapper);

        if (todayTasks.isEmpty()) {
            // 初始化今日任务
            List<DailyTask> allTasks = dailyTaskMapper.selectList(null);
            todayTasks = allTasks.stream().map(task -> {
                UserDailyTask userTask = new UserDailyTask();
                userTask.setUserId(userId);
                userTask.setTaskId(task.getId());
                userTask.setCreateDate(today);
                userTask.setCurrentCount(0);
                userTask.setIsCompleted(false);
                userTask.setRewardReceived(false);
                userDailyTaskMapper.insert(userTask);
                return userTask;
            }).collect(Collectors.toList());
        }
        return todayTasks;
    }

    @Override
    public boolean receiveReward(Long userId, Long taskId) {
        QueryWrapper<UserDailyTask> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("task_id", taskId).eq("create_date", LocalDate.now());
        UserDailyTask userTask = userDailyTaskMapper.selectOne(qw);

        if (userTask != null && userTask.getIsCompleted() && !userTask.getRewardReceived()) {
            userTask.setRewardReceived(true);
            userDailyTaskMapper.updateById(userTask);
            return true;
        }
        return false;
    }
}
