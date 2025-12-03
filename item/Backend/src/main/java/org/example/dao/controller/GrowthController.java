package org.example.dao.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dao.pojo.UserAchievement;
import org.example.dao.pojo.UserDailyTask;
import org.example.dao.service.DailyTaskService;
import org.example.dao.service.UserAchievementService;
import org.example.dao.service.UserBehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "成长与激励模块")
public class GrowthController {

    @Autowired
    private UserBehaviorService userBehaviorService;

    @Autowired
    private DailyTaskService dailyTaskService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Operation(summary = "获取用户行为统计（热力图数据）")
    @GetMapping("/behavior/stat")
    public List<Map<String, Object>> getBehaviorStats(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "month") String timeRange) {
        return userBehaviorService.getBehaviorStats(userId, timeRange);
    }

    @Operation(summary = "获取用户今日任务列表")
    @GetMapping("/task/daily")
    public List<UserDailyTask> getDailyTasks(@RequestParam Long userId) {
        return dailyTaskService.getTodayTasks(userId);
    }

    @Operation(summary = "领取每日任务奖励")
    @PostMapping("/task/receiveReward")
    public String receiveTaskReward(@RequestParam Long userId, @RequestParam Long taskId) {
        boolean success = dailyTaskService.receiveReward(userId, taskId);
        return success ? "奖励领取成功" : "奖励领取失败或已领取";
    }

    @Operation(summary = "获取用户成就列表")
    @GetMapping("/achievement/user")
    public List<UserAchievement> getUserAchievements(@RequestParam Long userId) {
        return userAchievementService.getUserAchievements(userId);
    }
}
