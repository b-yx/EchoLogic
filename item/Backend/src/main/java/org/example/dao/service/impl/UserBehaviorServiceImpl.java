package org.example.dao.service.impl;

import org.example.dao.mapper.UserBehaviorMapper;
import org.example.dao.pojo.UserBehavior;
import org.example.dao.service.DailyTaskService;
import org.example.dao.service.UserAchievementService;
import org.example.dao.service.UserBehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

@Service
public class UserBehaviorServiceImpl implements UserBehaviorService {

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Autowired
    private DailyTaskService dailyTaskService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Override
    @Transactional
    public void recordBehavior(Long userId, UserBehavior.BehaviorType type, Long relatedId) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setBehaviorType(type);
        behavior.setRelatedId(relatedId);
        behavior.setBehaviorTime(LocalDateTime.now());
        behavior.setCreateTime(LocalDateTime.now());
        userBehaviorMapper.insert(behavior);

        // --- 关键逻辑：关联更新任务和成就 ---
        dailyTaskService.updateTaskProgress(userId, type);
        userAchievementService.updateAchievementProgress(userId, type);
    }

    @Override
    public List<Map<String, Object>> getBehaviorStats(Long userId, String timeRange) {
        LocalDateTime endTime;
        LocalDateTime startTime;
        
        // 检查是否是YYYY-MM格式的月份查询
        if (timeRange.matches("\\d{4}-\\d{2}")) {
            // 解析年和月
            String[] parts = timeRange.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            
            // 设置为该月第一天的00:00:00
            startTime = LocalDateTime.of(year, month, 1, 0, 0, 0);
            
            // 设置为该月最后一天的23:59:59
            // 计算下个月第一天，然后减一天
            LocalDateTime nextMonthFirstDay = startTime.plusMonths(1);
            endTime = nextMonthFirstDay.minusSeconds(1);
        } else {
            endTime = LocalDateTime.now();
            // 根据时间范围设置查询起始时间
            switch (timeRange.toLowerCase()) {
                case "year":
                    startTime = endTime.minus(1, ChronoUnit.YEARS);
                    break;
                case "month":
                    startTime = endTime.minus(30, ChronoUnit.DAYS);
                    break;
                case "week":
                    startTime = endTime.minus(7, ChronoUnit.DAYS);
                    break;
                default:
                    startTime = endTime.minus(30, ChronoUnit.DAYS); // 默认近30天
            }
        }
        
        // 获取按天和行为类型分组的原始数据
        List<Map<String, Object>> rawData = userBehaviorMapper.countByDateRangeGroupedByDay(userId, startTime, endTime);
        
        // 转换为规范的数据格式
        Map<String, Map<String, Object>> dateMap = new HashMap<>();
        
        // 遍历原始数据，构建日期为键的映射
        for (Map<String, Object> item : rawData) {
            String date = (String) item.get("date");
            String type = (String) item.get("type");
            Long count = Long.valueOf(item.get("count").toString());
            
            // 确保日期记录存在
            if (!dateMap.containsKey(date)) {
                Map<String, Object> dateRecord = new HashMap<>();
                dateRecord.put("date", date);
                dateRecord.put("total", 0L);
                dateRecord.put("details", new HashMap<>());
                dateMap.put(date, dateRecord);
            }
            
            // 更新总计数
            Map<String, Object> dateRecord = dateMap.get(date);
            Long total = (Long) dateRecord.get("total");
            dateRecord.put("total", total + count);
            
            // 更新行为类型详情
            Map<String, Long> details = (Map<String, Long>) dateRecord.get("details");
            details.put(type, details.getOrDefault(type, 0L) + count);
        }
        
        // 生成完整的日期范围，确保没有遗漏
        LocalDate currentDate = endTime.toLocalDate();
        LocalDate startDate = startTime.toLocalDate();
        
        while (!startDate.isAfter(currentDate)) {
            String dateStr = startDate.format(DateTimeFormatter.ISO_DATE);
            if (!dateMap.containsKey(dateStr)) {
                Map<String, Object> dateRecord = new HashMap<>();
                dateRecord.put("date", dateStr);
                dateRecord.put("total", 0L);
                dateRecord.put("details", new HashMap<>());
                dateMap.put(dateStr, dateRecord);
            }
            startDate = startDate.plusDays(1);
        }
        
        // 转换为列表并按日期排序
        List<Map<String, Object>> result = new ArrayList<>(dateMap.values());
        result.sort(Comparator.comparing(m -> (String) m.get("date")));
        
        return result;
    }

    @Override
    public Map<String, Long> getItemBehaviorCounts(Long relatedId, Long userId) {
        List<Map<String, Object>> rows = userId == null
                ? userBehaviorMapper.countByRelatedId(relatedId)
                : userBehaviorMapper.countByRelatedIdAndUser(relatedId, userId);
        Map<String, Long> result = new java.util.HashMap<>();
        for (Map<String, Object> row : rows) {
            Object type = row.get("type");
            Object count = row.get("count");
            if (type != null && count != null) {
                result.put(String.valueOf(type), Long.valueOf(String.valueOf(count)));
            }
        }
        return result;
    }
}
