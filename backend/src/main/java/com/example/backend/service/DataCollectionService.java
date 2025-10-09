package com.example.backend.service;

import com.example.backend.entity.GitRepository;
import com.example.backend.entity.GitActivity;
import com.example.backend.mapper.GitRepositoryMapper;
import com.example.backend.mapper.GitActivityMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据收集服务类
 */
@Service
public class DataCollectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataCollectionService.class);
    
    @Autowired
    private GitRepositoryMapper gitRepositoryMapper;
    
    @Autowired
    private GitActivityMapper gitActivityMapper;
    
    /**
     * 获取数据收集统计信息
     * @return 统计数据
     */
    public Map<String, Object> getCollectionStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 总仓库数
            QueryWrapper<GitRepository> repoWrapper = new QueryWrapper<>();
            Long totalRepos = gitRepositoryMapper.selectCount(repoWrapper);
            stats.put("totalRepos", totalRepos != null ? totalRepos : 0);
            
            // 已同步仓库数
            QueryWrapper<GitRepository> syncedWrapper = new QueryWrapper<>();
            syncedWrapper.eq("sync_status", "success");
            Long syncedRepos = gitRepositoryMapper.selectCount(syncedWrapper);
            stats.put("syncedRepos", syncedRepos != null ? syncedRepos : 0);
            
            // 总活动记录数（提交、PR、Issues等）
            QueryWrapper<GitActivity> activityWrapper = new QueryWrapper<>();
            Long totalActivities = gitActivityMapper.selectCount(activityWrapper);
            stats.put("totalCommits", totalActivities != null ? totalActivities : 0);
            
            // 已处理数据数（与总活动记录数相同）
            stats.put("processedData", totalActivities != null ? totalActivities : 0);
            
            // 最近24小时的活动数
            QueryWrapper<GitActivity> recentWrapper = new QueryWrapper<>();
            recentWrapper.ge("created_at", LocalDateTime.now().minusDays(1));
            Long recentActivities = gitActivityMapper.selectCount(recentWrapper);
            stats.put("recentActivities", recentActivities != null ? recentActivities : 0);
            
            logger.info("获取数据收集统计信息成功: {}", stats);
            
        } catch (Exception e) {
            logger.error("获取数据收集统计信息失败: {}", e.getMessage(), e);
            // 返回默认值
            stats.put("totalRepos", 0);
            stats.put("syncedRepos", 0);
            stats.put("totalCommits", 0);
            stats.put("processedData", 0);
            stats.put("recentActivities", 0);
        }
        
        return stats;
    }
    
    /**
     * 获取数据收集任务状态
     * @return 任务列表
     */
    public List<Map<String, Object>> getCollectionTasks() {
        List<Map<String, Object>> tasks = new ArrayList<>();
        
        try {
            // 获取所有仓库的同步状态
            List<GitRepository> repositories = gitRepositoryMapper.selectList(null);
            
            for (GitRepository repo : repositories) {
                Map<String, Object> task = new HashMap<>();
                task.put("id", repo.getRepositoryId());
                task.put("name", repo.getRepositoryName());
                task.put("status", repo.getSyncStatus() != null ? repo.getSyncStatus() : "pending");
                
                // 计算进度（基于同步状态）
                int progress = 0;
                String status = repo.getSyncStatus();
                if ("success".equals(status)) {
                    progress = 100;
                } else if ("syncing".equals(status)) {
                    progress = 50;
                } else if ("failed".equals(status)) {
                    progress = 0;
                } else {
                    progress = 0; // pending状态
                }
                task.put("progress", progress);
                
                // 添加最后同步时间
                if (repo.getLastSyncTime() != null) {
                    task.put("lastSyncTime", repo.getLastSyncTime().toString());
                }
                
                tasks.add(task);
            }
            
            logger.info("获取数据收集任务状态成功，共{}个任务", tasks.size());
            
        } catch (Exception e) {
            logger.error("获取数据收集任务状态失败: {}", e.getMessage(), e);
        }
        
        return tasks;
    }
    
    /**
     * 获取数据收集日志
     * @return 日志列表
     */
    public List<Map<String, Object>> getCollectionLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();
        
        try {
            // 获取最近的活动记录作为日志
            QueryWrapper<GitActivity> wrapper = new QueryWrapper<>();
            wrapper.orderByDesc("created_at");
            wrapper.last("LIMIT 50"); // 限制最近50条记录
            
            List<GitActivity> activities = gitActivityMapper.selectList(wrapper);
            
            for (GitActivity activity : activities) {
                Map<String, Object> log = new HashMap<>();
                log.put("timestamp", activity.getCreatedAt().toString());
                log.put("level", "info");
                log.put("message", String.format("处理%s活动: %s", 
                    activity.getActivityType(), 
                    activity.getAuthorName() != null ? activity.getAuthorName() : "未知用户"));
                log.put("repositoryId", activity.getRepositoryId());
                logs.add(log);
            }
            
            logger.info("获取数据收集日志成功，共{}条日志", logs.size());
            
        } catch (Exception e) {
            logger.error("获取数据收集日志失败: {}", e.getMessage(), e);
        }
        
        return logs;
    }
    
    /**
     * 获取完整的数据收集状态
     * @return 完整状态信息
     */
    public Map<String, Object> getDataCollectionStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("stats", getCollectionStats());
        status.put("tasks", getCollectionTasks());
        status.put("logs", getCollectionLogs());
        
        return status;
    }
}