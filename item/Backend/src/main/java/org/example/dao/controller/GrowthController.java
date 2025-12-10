package org.example.dao.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dao.dto.ApiResponse;
import org.example.dao.pojo.UserAchievement;
import org.example.dao.pojo.UserDailyTask;
import org.example.dao.service.DailyTaskService;
import org.example.dao.service.UserAchievementService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "用户成长与激励")
public class GrowthController {

    private static final Logger log = LoggerFactory.getLogger(GrowthController.class);

    

    @Autowired
    private DailyTaskService dailyTaskService;

    @Autowired
    private UserAchievementService userAchievementService;

    



    

    @Operation(summary = "获取用户今日任务列表")
    @GetMapping("/task/daily")
    public ApiResponse<List<UserDailyTask>> getDailyTasks(@RequestParam Long userId) {
        return ApiResponse.ok(dailyTaskService.getTodayTasks(userId));
    }

    @Operation(summary = "领取每日任务奖励")
    @PostMapping("/task/receiveReward")
    public ApiResponse<Map<String, Object>> receiveTaskReward(@RequestParam Long userId, @RequestParam Long taskId) {
        boolean success = dailyTaskService.receiveReward(userId, taskId);
        if (success) {
            return ApiResponse.ok("奖励领取成功", java.util.Map.of("rewardReceived", true));
        } else {
            return ApiResponse.error(400, "奖励领取失败或已领取");
        }
    }

    @Operation(summary = "获取用户成就列表")
    @GetMapping("/achievement/user")
    public ApiResponse<List<UserAchievement>> getUserAchievements(@RequestParam Long userId) {
        return ApiResponse.ok(userAchievementService.getUserAchievements(userId));
    }

    


}
