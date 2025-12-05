package org.example.dao.service;

public interface UserQuotaService {
    long getQuota(Long userId);
    void addQuota(Long userId, int amount);
}
