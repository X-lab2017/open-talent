package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.GitActivity;
import com.example.backend.entity.GitRepository;
import com.example.backend.mapper.GitActivityMapper;
import com.example.backend.mapper.GitRepositoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Git数据采集服务类
 */
@Service
public class GitDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(GitDataService.class);
    
    @Autowired
    private GitRepositoryMapper gitRepositoryMapper;
    
    @Autowired
    private GitActivityMapper gitActivityMapper;
    
    @Autowired
    private GitHubDataSyncService gitHubDataSyncService;
    
    /**
     * 添加Git仓库
     * @param gitRepository Git仓库信息
     * @return 添加结果
     */
    @Transactional
    public boolean addGitRepository(GitRepository gitRepository) {
        gitRepository.setCreatedAt(LocalDateTime.now());
        gitRepository.setUpdatedAt(LocalDateTime.now());
        if (gitRepository.getSyncStatus() == null) {
            gitRepository.setSyncStatus("pending");
        }
        return gitRepositoryMapper.insert(gitRepository) > 0;
    }
    
    /**
     * 更新Git仓库信息
     * @param gitRepository Git仓库信息
     * @return 更新结果
     */
    @Transactional
    public boolean updateGitRepository(GitRepository gitRepository) {
        gitRepository.setUpdatedAt(LocalDateTime.now());
        return gitRepositoryMapper.updateById(gitRepository) > 0;
    }
    
    /**
     * 删除Git仓库
     * @param repositoryId 仓库ID
     * @return 删除结果
     */
    @Transactional
    public boolean deleteGitRepository(Integer repositoryId) {
        // 同时删除相关的活动记录
        QueryWrapper<GitActivity> wrapper = new QueryWrapper<>();
        wrapper.eq("repo_id", repositoryId);
        gitActivityMapper.delete(wrapper);
        
        return gitRepositoryMapper.deleteById(repositoryId) > 0;
    }
    
    /**
     * 获取所有Git仓库列表
     * @return Git仓库列表
     */
    public List<GitRepository> getAllRepositories() {
        return gitRepositoryMapper.selectList(null);
    }
    
    /**
     * 获取所有启用的Git仓库列表
     * @return 启用的Git仓库列表
     */
    public List<GitRepository> getActiveRepositories() {
        QueryWrapper<GitRepository> wrapper = new QueryWrapper<>();
        wrapper.eq("is_active", 1);
        return gitRepositoryMapper.selectList(wrapper);
    }
    
    /**
     * 根据课程ID获取Git仓库列表
     * @param courseId 课程ID
     * @return Git仓库列表
     */
    public List<GitRepository> getRepositoriesByCourseId(Integer courseId) {
        return gitRepositoryMapper.selectByCourseId(courseId);
    }
    
    /**
     * 根据平台类型获取Git仓库列表
     * @param courseId 课程ID
     * @param platform 平台类型
     * @return Git仓库列表
     */
    public List<GitRepository> getRepositoriesByPlatform(Integer courseId, String platform) {
        return gitRepositoryMapper.selectByCourseIdAndPlatform(courseId, platform);
    }
    
    /**
     * 根据仓库URL查询Git仓库
     * @param repositoryUrl 仓库URL
     * @return Git仓库信息
     */
    public GitRepository getRepositoryByUrl(String repositoryUrl) {
        return gitRepositoryMapper.selectByRepoUrl(repositoryUrl);
    }
    
    /**
     * 更新仓库最后同步时间
     * @param repositoryId 仓库ID
     * @return 更新结果
     */
    @Transactional
    public boolean updateLastSyncTime(Integer repositoryId) {
        return gitRepositoryMapper.updateLastSyncAt(repositoryId, LocalDateTime.now()) > 0;
    }
    
    /**
     * 添加Git活动记录
     * @param gitActivity Git活动记录
     * @return 添加结果
     */
    @Transactional
    public boolean addGitActivity(GitActivity gitActivity) {
        gitActivity.setCreatedAt(LocalDateTime.now());
        return gitActivityMapper.insert(gitActivity) > 0;
    }
    
    /**
     * 批量添加Git活动记录
     * @param activities Git活动记录列表
     * @return 添加结果
     */
    @Transactional
    public boolean batchAddGitActivities(List<GitActivity> activities) {
        try {
            for (GitActivity activity : activities) {
                activity.setCreatedAt(LocalDateTime.now());
                gitActivityMapper.insert(activity);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("批量添加Git活动记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据仓库ID分页查询Git活动记录
     * @param repositoryId 仓库ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return Git活动记录分页数据
     */
    public IPage<GitActivity> getActivitiesByRepositoryId(Integer repositoryId, Integer pageNum, Integer pageSize) {
        Page<GitActivity> page = new Page<>(pageNum, pageSize);
        return gitActivityMapper.selectByRepoId(page, repositoryId);
    }
    
    /**
     * 根据成员ID查询Git活动记录
     * @param memberId 成员ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return Git活动记录列表
     */
    public List<GitActivity> getActivitiesByMemberId(Integer memberId, LocalDateTime startTime, LocalDateTime endTime) {
        return gitActivityMapper.selectByMemberIdAndTimeRange(memberId, startTime, endTime);
    }
    
    /**
     * 根据活动类型查询Git活动记录
     * @param repositoryId 仓库ID
     * @param activityType 活动类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return Git活动记录列表
     */
    public List<GitActivity> getActivitiesByType(Integer repositoryId, String activityType, LocalDateTime startTime, LocalDateTime endTime) {
        return gitActivityMapper.selectByRepoIdAndActivityType(repositoryId, activityType);
    }
    
    /**
     * 获取成员在指定时间段内的提交统计
     * @param memberId 成员ID
     * @param repositoryId 仓库ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 提交统计数据
     */
    public GitActivity getMemberCommitStats(Integer memberId, Integer repositoryId, LocalDateTime startTime, LocalDateTime endTime) {
        return gitActivityMapper.selectMemberCommitStats(memberId, repositoryId, startTime, endTime);
    }
    
    /**
     * 删除指定时间之前的Git活动记录
     * @param beforeTime 时间点
     * @return 删除结果
     */
    @Transactional
    public boolean deleteActivitiesBefore(LocalDateTime beforeTime) {
        QueryWrapper<GitActivity> wrapper = new QueryWrapper<>();
        wrapper.lt("activity_time", beforeTime);
        return gitActivityMapper.delete(wrapper) > 0;
    }
    
    /**
     * 同步Git仓库数据
     * @param repositoryId 仓库ID
     * @return 同步结果
     */
    @Transactional
    public boolean syncRepositoryData(Integer repositoryId) {
        try {
            logger.info("开始同步仓库数据，仓库ID: {}", repositoryId);
            
            GitRepository repository = gitRepositoryMapper.selectById(repositoryId);
            if (repository == null) {
                logger.warn("仓库不存在，仓库ID: {}", repositoryId);
                return false;
            }
            
            // 更新同步状态为进行中
            repository.setSyncStatus("syncing");
            repository.setUpdatedAt(LocalDateTime.now());
            gitRepositoryMapper.updateById(repository);
            
            boolean syncResult = false;
            
            // 根据平台类型选择同步方式
            if ("github".equalsIgnoreCase(repository.getPlatform())) {
                // 使用GitHub API同步数据
                syncResult = gitHubDataSyncService.syncRepositoryData(repository);
            } else {
                logger.warn("暂不支持的平台类型: {}", repository.getPlatform());
                // 对于其他平台，可以在这里添加相应的同步逻辑
            }
            
            // 更新同步状态和时间
            if (syncResult) {
                repository.setSyncStatus("success");
                // 更新最后同步时间
                repository.setLastSyncTime(LocalDateTime.now());
                logger.info("仓库数据同步成功，仓库ID: {}", repositoryId);
            } else {
                repository.setSyncStatus("failed");
                logger.error("仓库数据同步失败，仓库ID: {}", repositoryId);
            }
            
            repository.setUpdatedAt(LocalDateTime.now());
            gitRepositoryMapper.updateById(repository);
            
            return syncResult;
            
        } catch (Exception e) {
            logger.error("同步Git仓库数据异常，仓库ID: {}, 错误信息: {}", repositoryId, e.getMessage(), e);
            
            // 更新同步状态为失败
            try {
                GitRepository repository = gitRepositoryMapper.selectById(repositoryId);
                if (repository != null) {
                    repository.setSyncStatus("failed");
                    repository.setUpdatedAt(LocalDateTime.now());
                    gitRepositoryMapper.updateById(repository);
                }
            } catch (Exception updateException) {
                logger.error("更新同步状态失败: {}", updateException.getMessage());
            }
            
            throw new RuntimeException("同步Git仓库数据失败: " + e.getMessage(), e);
        }
    }
}