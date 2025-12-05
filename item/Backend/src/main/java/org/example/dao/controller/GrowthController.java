package org.example.dao.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dao.dto.ApiResponse;
import org.example.dao.pojo.UserAchievement;
import org.example.dao.pojo.UserDailyTask;
import org.example.dao.service.DailyTaskService;
import org.example.dao.service.UserAchievementService;
import org.example.dao.service.UserBehaviorService;
import org.example.dao.pojo.UserBehavior;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "用户成长与激励")
public class GrowthController {

    @Autowired
    private UserBehaviorService userBehaviorService;

    @Autowired
    private DailyTaskService dailyTaskService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Value("${features.behavior-record-test-enabled:true}")
    private boolean behaviorRecordTestEnabled;

    @Autowired
    private org.example.dao.service.UserQuotaService userQuotaService;

    @Operation(summary = "【测试用】记录一次用户行为")
    @PostMapping("/behavior/record")
    public ApiResponse<Map<String, Object>> recordUserBehavior(
            @RequestParam Long userId,
            @RequestParam UserBehavior.BehaviorType behaviorType,
            @RequestParam(required = false) Long relatedId) {
        if (!behaviorRecordTestEnabled) {
            return ApiResponse.error(404, "接口已禁用");
        }
        userBehaviorService.recordBehavior(userId, behaviorType, relatedId);
        return ApiResponse.ok("记录成功", java.util.Map.of("behaviorType", behaviorType.name()));
    }

    @Operation(summary = "获取用户行为统计（热力图数据）")
    @GetMapping("/behavior/stat")
    public ApiResponse<List<Map<String, Object>>> getBehaviorStats(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "month") String timeRange) {
        return ApiResponse.ok(userBehaviorService.getBehaviorStats(userId, timeRange));
    }

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

    @Operation(summary = "获取指定对象的行为统计")
    @GetMapping("/behavior/itemCounts")
    public ApiResponse<Map<String, Long>> getItemCounts(@RequestParam Long relatedId,
                                           @RequestParam(required = false) Long userId) {
        return ApiResponse.ok(userBehaviorService.getItemBehaviorCounts(relatedId, userId));
    }

    @Operation(summary = "查询用户AI配额")
    @GetMapping("/user/quota")
    public ApiResponse<Map<String, Object>> getUserQuota(@RequestParam Long userId) {
        long quota = userQuotaService.getQuota(userId);
        return ApiResponse.ok(java.util.Map.of("userId", userId, "quota", quota));
    }
}
