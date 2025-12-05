package org.example.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.dao.mapper.UserQuotaMapper;
import org.example.dao.pojo.UserQuota;
import org.example.dao.service.UserQuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserQuotaServiceImpl implements UserQuotaService {

    @Autowired
    private UserQuotaMapper userQuotaMapper;

    @Override
    public long getQuota(Long userId) {
        QueryWrapper<UserQuota> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        UserQuota q = userQuotaMapper.selectOne(qw);
        return q == null ? 0L : q.getQuota();
    }

    @Override
    public void addQuota(Long userId, int amount) {
        QueryWrapper<UserQuota> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        UserQuota q = userQuotaMapper.selectOne(qw);
        if (q == null) {
            q = new UserQuota();
            q.setUserId(userId);
            q.setQuota(amount);
            userQuotaMapper.insert(q);
        } else {
            q.setQuota(q.getQuota() + amount);
            userQuotaMapper.updateById(q);
        }
    }
}
