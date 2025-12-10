package org.example.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.dao.mapper.UserAchievementMapper;
import org.example.dao.pojo.UserAchievement;
import org.example.dao.service.UserAchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAchievementServiceImpl implements UserAchievementService {

    @Autowired
    private UserAchievementMapper userAchievementMapper;

    @Override
    public List<UserAchievement> getUserAchievements(Long userId) {
        QueryWrapper<UserAchievement> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        return userAchievementMapper.selectList(qw);
    }
}
