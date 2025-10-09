package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.GitActivity;
import com.example.backend.entity.GitRepository;
import com.example.backend.entity.Member;
import com.example.backend.mapper.GitActivityMapper;
import com.example.backend.mapper.MemberMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GitHub数据同步服务类
 */
@Service
public class GitHubDataSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(GitHubDataSyncService.class);
    
    @Autowired
    private GitHubApiService gitHubApiService;
    
    @Autowired
    private GitActivityMapper gitActivityMapper;
    
    @Autowired
    private MemberMapper memberMapper;

    /**
     * 通过GitHub用户名查找成员ID
     * @param githubUsername GitHub用户名
     * @return 成员ID，如果未找到则返回null
     */
    private Integer findMemberIdByGithubUsername(String githubUsername) {
        if (!StringUtils.hasText(githubUsername)) {
            return null;
        }
        
        try {
            LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Member::getGithubAccount, githubUsername);
            Member member = memberMapper.selectOne(queryWrapper);
            
            if (member != null) {
                logger.debug("成功匹配GitHub用户到系统成员: {} -> 成员ID: {}", githubUsername, member.getMemberId());
                return member.getMemberId();
            } else {
                logger.info("未找到对应的系统成员，GitHub用户: {} 将保存为未关联用户", githubUsername);
                return null;
            }
        } catch (Exception e) {
            logger.warn("通过GitHub用户名查找成员失败: {}, 错误: {}", githubUsername, e.getMessage());
            return null;
        }
    }

    /**
     * 同步仓库数据同步仓库的所有数据 - 基于GitHub API实际支持的数据维度
     * @param repository Git仓库信息
     * @return 同步结果
     */
    @Transactional
    public boolean syncRepositoryData(GitRepository repository) {
        try {
            String[] repoInfo = gitHubApiService.parseRepoUrl(repository.getRepositoryUrl());
            String owner = repoInfo[0];
            String repo = repoInfo[1];
            Integer repositoryId = repository.getRepositoryId();
            Integer courseId = repository.getCourseId();
            
            logger.info("开始同步GitHub仓库数据: {}/{}, 课程ID: {}", owner, repo, courseId);
            
            // 1. 同步提交统计数据 (Repository Statistics API)
            syncCommitStatistics(owner, repo, repositoryId, courseId);
            
            // 2. 同步Pull Request数据
            syncPullRequests(owner, repo, repositoryId, courseId);
            
            // 3. 同步Issues数据
            syncIssues(repositoryId, owner, repo, courseId);
            
            // 4. 同步Releases数据
            syncReleases(repositoryId, owner, repo, courseId);
            
            // 5. 同步分支和标签数据
            syncBranchesAndTags(repositoryId, owner, repo, courseId);
            
            // 6. 同步基础提交记录（保持原有功能）
            int basicCommits = syncBasicCommits(owner, repo, repositoryId, courseId);
            
            logger.info("GitHub仓库数据同步完成: {}/{}, 基础提交记录: {}条", owner, repo, basicCommits);
            return true;
            
        } catch (Exception e) {
            logger.error("同步GitHub仓库数据失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 同步提交统计数据
     */
    private void syncCommitStatistics(String owner, String repo, Integer repositoryId, Integer courseId) {
        try {
            logger.info("开始同步提交统计数据: {}/{}", owner, repo);
            
            // 获取贡献者统计
            try {
                JsonNode contributorStats = gitHubApiService.getContributorStatistics(owner, repo, courseId);
                if (contributorStats != null && contributorStats.isArray() && contributorStats.size() > 0) {
                    processContributorStatistics(contributorStats, repositoryId);
                    logger.info("处理了{}个贡献者统计", contributorStats.size());
                }
            } catch (Exception e) {
                logger.warn("获取贡献者统计失败: {}", e.getMessage());
            }
            
            // 获取提交活动统计
            try {
                JsonNode commitActivity = gitHubApiService.getCommitActivity(owner, repo, courseId);
                if (commitActivity != null && commitActivity.isArray() && commitActivity.size() > 0) {
                    processCommitActivity(commitActivity, repositoryId);
                    logger.info("处理了{}周的提交活动数据", commitActivity.size());
                }
            } catch (Exception e) {
                logger.warn("获取提交活动统计失败: {}", e.getMessage());
            }
            
            // 获取代码频率统计
            try {
                JsonNode codeFrequency = gitHubApiService.getCodeFrequency(owner, repo, courseId);
                if (codeFrequency != null && codeFrequency.isArray() && codeFrequency.size() > 0) {
                    processCodeFrequency(codeFrequency, repositoryId);
                    logger.info("处理了{}周的代码频率数据", codeFrequency.size());
                }
            } catch (Exception e) {
                logger.warn("获取代码频率统计失败: {}", e.getMessage());
            }
            
            // 获取参与度统计
            try {
                JsonNode participation = gitHubApiService.getParticipation(owner, repo, courseId);
                if (participation != null) {
                    processParticipation(participation, repositoryId);
                    logger.info("处理了参与度统计数据");
                }
            } catch (Exception e) {
                logger.warn("获取参与度统计失败: {}", e.getMessage());
            }
            
            logger.info("提交统计数据同步完成: {}/{}", owner, repo);
        } catch (Exception e) {
            logger.error("同步提交统计数据失败: {}/{}, 错误: {}", owner, repo, e.getMessage(), e);
        }
    }
    
    /**
     * 同步Pull Request数据
     */
    private void syncPullRequests(String owner, String repo, Integer repositoryId, Integer courseId) {
        try {
            logger.info("开始同步Pull Request数据: {}/{}", owner, repo);
            
            // 获取不同状态的PR数据
            String[] states = {"open", "closed", "all"};
            int totalProcessed = 0;
            
            for (String state : states) {
                try {
                    List<JsonNode> pullRequestsList = gitHubApiService.getPullRequests(owner, repo, state, courseId);
                    if (pullRequestsList != null && !pullRequestsList.isEmpty()) {
                        // 将List<JsonNode>转换为JsonNode数组
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode pullRequests = mapper.valueToTree(pullRequestsList);
                        processPullRequests(pullRequests, repositoryId);
                        totalProcessed += pullRequestsList.size();
                        logger.info("处理{}状态PR: {}个", state, pullRequestsList.size());
                    }
                } catch (Exception e) {
                    logger.warn("获取{}状态PR失败: {}", state, e.getMessage());
                }
            }
            
            logger.info("Pull Request数据同步完成: {}/{}, 共处理{}个PR", owner, repo, totalProcessed);
        } catch (Exception e) {
            logger.error("同步Pull Request数据失败: {}/{}, 错误: {}", owner, repo, e.getMessage(), e);
        }
    }
    
    /**
     * 同步Issues数据
     */
    public void syncIssues(Integer repositoryId, String owner, String repo, Integer courseId) {
        try {
            logger.info("开始同步Issues数据: {}/{}", owner, repo);
            
            int totalProcessed = 0;
            
            // 获取不同状态的Issues数据
            String[] states = {"open", "closed", "all"};
            
            for (String state : states) {
                try {
                    List<JsonNode> issuesList = gitHubApiService.getIssues(owner, repo, state, courseId);
                    if (issuesList != null && !issuesList.isEmpty()) {
                        // 将List<JsonNode>转换为JsonNode数组
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode issues = mapper.valueToTree(issuesList);
                        processIssues(issues, repositoryId);
                        totalProcessed += issuesList.size();
                        logger.info("处理了{}个{}状态的Issues", issuesList.size(), state);
                    }
                } catch (Exception e) {
                    logger.warn("获取{}状态Issues数据失败: {}", state, e.getMessage());
                }
            }
            
            logger.info("Issues数据同步完成，总共处理了{}条记录", totalProcessed);
            
        } catch (Exception e) {
            logger.error("同步Issues数据失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 同步Releases数据
     */
    public void syncReleases(Integer repositoryId, String owner, String repo, Integer courseId) {
        try {
            logger.info("开始同步Releases数据: {}/{}", owner, repo);
            
            List<JsonNode> releasesList = gitHubApiService.getReleases(owner, repo, courseId);
            if (releasesList != null && !releasesList.isEmpty()) {
                // 将List<JsonNode>转换为JsonNode数组
                ObjectMapper mapper = new ObjectMapper();
                JsonNode releases = mapper.valueToTree(releasesList);
                processReleases(releases, repositoryId);
                logger.info("处理了{}个Releases", releasesList.size());
            } else {
                logger.info("未找到Releases数据");
            }
            
            logger.info("Releases数据同步完成");
            
        } catch (Exception e) {
            logger.error("同步Releases数据失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 同步分支和标签数据
     */
    public void syncBranchesAndTags(Integer repositoryId, String owner, String repo, Integer courseId) {
        try {
            logger.info("开始同步分支和标签数据: {}/{}", owner, repo);
            
            int totalProcessed = 0;
            
            // 同步分支数据
            try {
                List<JsonNode> branchesList = gitHubApiService.getBranches(owner, repo, courseId);
                if (branchesList != null && !branchesList.isEmpty()) {
                    // 将List<JsonNode>转换为JsonNode数组
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode branches = mapper.valueToTree(branchesList);
                    processBranches(branches, repositoryId);
                    totalProcessed += branchesList.size();
                    logger.info("处理了{}个分支", branchesList.size());
                } else {
                    logger.info("未找到分支数据");
                }
            } catch (Exception e) {
                logger.warn("获取分支数据失败: {}", e.getMessage());
            }
            
            // 同步标签数据
            try {
                List<JsonNode> tagsList = gitHubApiService.getTags(owner, repo, courseId);
                if (tagsList != null && !tagsList.isEmpty()) {
                    // 将List<JsonNode>转换为JsonNode数组
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode tags = mapper.valueToTree(tagsList);
                    processTags(tags, repositoryId);
                    totalProcessed += tagsList.size();
                    logger.info("处理了{}个标签", tagsList.size());
                } else {
                    logger.info("未找到标签数据");
                }
            } catch (Exception e) {
                logger.warn("获取标签数据失败: {}", e.getMessage());
            }
            
            logger.info("分支和标签数据同步完成，总共处理了{}条记录", totalProcessed);
            
        } catch (Exception e) {
            logger.error("同步分支和标签数据失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 同步基础提交记录（保持原有功能）
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param repoId 仓库ID
     * @param courseId 课程ID
     * @return 同步的活动记录数量
     */
    private int syncBasicCommits(String owner, String repo, Integer repoId, Integer courseId) {
        try {
            // 获取最近30天的提交记录
            LocalDateTime since = LocalDateTime.now().minusDays(30);
            List<JsonNode> commits = gitHubApiService.getCommits(owner, repo, since, null, courseId);
            
            // 转换并保存提交记录
            List<GitActivity> activities = convertCommitsToActivities(commits, repoId);
            saveActivities(activities);
            
            return activities.size();
        } catch (Exception e) {
            logger.warn("同步基础提交记录失败: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * 将GitHub提交记录转换为GitActivity实体
     * @param commits GitHub提交记录
     * @param repoId 仓库ID
     * @return GitActivity列表
     */
    private List<GitActivity> convertCommitsToActivities(List<JsonNode> commits, Integer repoId) {
        List<GitActivity> activities = new ArrayList<>();
        
        for (JsonNode commit : commits) {
            try {
                GitActivity activity = new GitActivity();
                activity.setRepositoryId(repoId.longValue());
                activity.setActivityType("commit");
                
                // 设置提交信息和时间
                JsonNode commitInfo = commit.path("commit");
                if (!commitInfo.isMissingNode()) {
                    activity.setCommitMessage(commitInfo.path("message").asText());
                    
                    // 解析提交时间
                    String dateStr = commitInfo.path("author").path("date").asText();
                    if (!dateStr.isEmpty()) {
                        try {
                            activity.setActivityTime(LocalDateTime.parse(dateStr.replace("Z", ""), 
                                DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        } catch (Exception e) {
                            logger.warn("解析提交时间失败: {}", dateStr);
                            activity.setActivityTime(LocalDateTime.now());
                        }
                    } else {
                        activity.setActivityTime(LocalDateTime.now());
                    }
                }
                
                // 提取作者信息并设置member_id
                JsonNode author = commit.get("author");
                if (author != null) {
                    String githubUsername = getTextValue(author, "login");
                    if (StringUtils.hasText(githubUsername)) {
                        Integer memberId = findMemberIdByGithubUsername(githubUsername);
                        if (memberId != null) {
                            activity.setMemberId(memberId.longValue());
                            logger.debug("提交记录已关联到系统成员: {} -> 成员ID: {}", githubUsername, memberId);
                        } else {
                            logger.info("提交记录保存为未关联用户: GitHub用户={}, 提交SHA={}", githubUsername, activity.getCommitHash());
                        }
                        activity.setAuthorName(githubUsername);
                    }
                    activity.setAuthorEmail(getTextValue(author, "email"));
                }
                
                // 提取SHA
                activity.setCommitHash(getTextValue(commit, "sha"));
                
                // 提取统计信息
                JsonNode stats = commit.get("stats");
                if (stats != null) {
                    activity.setLinesAdded(getIntValue(stats, "additions"));
                    activity.setLinesDeleted(getIntValue(stats, "deletions"));
                }
                
                // 设置创建时间
                activity.setCreatedAt(LocalDateTime.now());
                
                activities.add(activity);
                
            } catch (Exception e) {
                logger.warn("转换提交记录失败: {}", e.getMessage());
            }
        }
        
        return activities;
    }
    
    /**
     * 批量保存活动记录
     * @param activities 活动记录列表
     */
    private void saveActivities(List<GitActivity> activities) {
        for (GitActivity activity : activities) {
            try {
                // 检查是否已存在相同的提交记录
                if (StringUtils.hasText(activity.getCommitHash())) {
                    List<GitActivity> existing = gitActivityMapper.selectByCommitHash(activity.getCommitHash());
                    if (existing.isEmpty()) {
                        gitActivityMapper.insert(activity);
                    }
                }
            } catch (Exception e) {
                logger.warn("保存活动记录失败: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 处理贡献者统计数据
     * @param contributors 贡献者列表
     * @param repoId 仓库ID
     * @return 处理的记录数量
     */
    private int processContributorStatistics(JsonNode contributors, Integer repoId) {
        int count = 0;
        try {
            logger.info("处理贡献者统计数据，共{}个贡献者", contributors.size());
            
            // 清理旧的贡献者统计数据
            try {
                int deletedCount = gitActivityMapper.deleteByRepoIdAndActivityType(repoId.longValue(), "contributor_statistics");
                logger.info("清理了{}条旧的贡献者统计数据", deletedCount);
            } catch (Exception e) {
                logger.warn("清理旧贡献者统计数据失败: {}", e.getMessage());
            }
            
            for (JsonNode contributor : contributors) {
                int retryCount = 0;
                int maxRetries = 3;
                boolean success = false;
                
                while (retryCount < maxRetries && !success) {
                    try {
                        GitActivity activity = new GitActivity();
                        activity.setRepositoryId(repoId.longValue());
                        activity.setActivityType("contributor_statistics");
                        
                        // 提取统计数据
                        Integer totalCommits = getIntValue(contributor, "total");
                        
                        // 提取贡献者信息
                        JsonNode author = contributor.get("author");
                        if (author != null) {
                            String login = getTextValue(author, "login");
                            String name = getTextValue(author, "name");
                            
                            // 通过GitHub用户名查找并设置member_id
                            if (StringUtils.hasText(login)) {
                                Integer memberId = findMemberIdByGithubUsername(login);
                                if (memberId != null) {
                                    activity.setMemberId(memberId.longValue());
                                    logger.debug("贡献者统计已关联到系统成员: {} -> 成员ID: {}", login, memberId);
                                } else {
                                    logger.info("贡献者统计保存为未关联用户: GitHub用户={}, 总提交数={}", login, totalCommits);
                                }
                            }
                            
                            // 构建元数据包含作者信息和总提交数
                            String metadata = String.format("{\"author\":{\"login\":\"%s\",\"name\":\"%s\"},\"total_commits\":%d}", 
                                login != null ? login : "", name != null ? name : "", totalCommits != null ? totalCommits : 0);
                            activity.setMetadata(metadata);
                        }
                        
                        // 处理周统计数据
                        JsonNode weeks = contributor.get("weeks");
                        if (weeks != null && weeks.isArray() && weeks.size() > 0) {
                            // 取最近一周的数据作为活动时间
                            JsonNode lastWeek = weeks.get(weeks.size() - 1);
                            long timestamp = getIntValue(lastWeek, "w");
                            if (timestamp > 0) {
                                activity.setActivityTime(LocalDateTime.ofEpochSecond(timestamp, 0, java.time.ZoneOffset.UTC));
                            } else {
                                activity.setActivityTime(LocalDateTime.now());
                            }
                            
                            activity.setLinesAdded(getIntValue(lastWeek, "a"));
                            activity.setLinesDeleted(getIntValue(lastWeek, "d"));
                        } else {
                            activity.setActivityTime(LocalDateTime.now());
                        }
                        
                        activity.setCreatedAt(LocalDateTime.now());
                        
                        // 检查是否重复
                        if (!isDuplicateActivity(activity)) {
                            gitActivityMapper.insert(activity);
                            count++;
                            logger.debug("贡献者统计: 总提交数: {}", totalCommits);
                        } else {
                            logger.debug("跳过重复的贡献者统计数据: {}", activity.getMetadata());
                        }
                        
                        success = true;
                        
                    } catch (Exception e) {
                        retryCount++;
                        if (retryCount >= maxRetries) {
                            logger.error("处理单个贡献者统计失败，已重试{}次: {}", maxRetries, e.getMessage());
                        } else {
                            logger.warn("处理单个贡献者统计失败，第{}次重试: {}", retryCount, e.getMessage());
                            try {
                                Thread.sleep(1000 * retryCount); // 递增延迟
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                }
            }
            
            logger.info("贡献者统计数据处理完成，共处理{}条记录", count);
        } catch (Exception e) {
            logger.error("处理贡献者统计数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("贡献者统计数据处理失败", e);
        }
        return count;
    }
    
    /**
     * 处理提交活动数据
     * @param commitActivity 提交活动数据
     * @param repositoryId 仓库ID
     * @return 处理的记录数量
     */
    private int processCommitActivity(JsonNode commitActivity, Integer repositoryId) {
        int count = 0;
        try {
            logger.info("处理提交活动数据，共{}周数据", commitActivity.size());
            
            for (JsonNode weekData : commitActivity) {
                GitActivity activity = new GitActivity();
                activity.setRepositoryId(repositoryId.longValue());
                activity.setActivityType("commit_activity");
                
                // 提取周时间戳
                long timestamp = getIntValue(weekData, "week");
                if (timestamp > 0) {
                    activity.setActivityTime(LocalDateTime.ofEpochSecond(timestamp, 0, java.time.ZoneOffset.UTC));
                } else {
                    activity.setActivityTime(LocalDateTime.now());
                }
                
                // 提取提交数量
                int totalCommits = getIntValue(weekData, "total");
                
                // 设置统计字段
                activity.setWeeklyCommits(totalCommits);
                activity.setTotalCommits(totalCommits);
                
                // 处理每日提交数据
                JsonNode days = weekData.get("days");
                if (days != null && days.isArray()) {
                    StringBuilder dayStats = new StringBuilder();
                    for (int i = 0; i < days.size(); i++) {
                        if (i > 0) dayStats.append(",");
                        dayStats.append(days.get(i).asInt());
                    }
                    
                    // 保存元数据
                    String metadata = String.format("{\"week\":%d,\"total\":%d,\"days\":[%s]}", 
                        timestamp, totalCommits, dayStats.toString());
                    activity.setMetadata(metadata);
                }
                
                activity.setCreatedAt(LocalDateTime.now());
                gitActivityMapper.insert(activity);
                count++;
            }
            
            logger.info("提交活动数据处理完成，共处理{}条记录", count);
        } catch (Exception e) {
            logger.error("处理提交活动数据失败: {}", e.getMessage(), e);
        }
        return count;
    }
    
    /**
     * 处理代码频率数据
     */
    private void processCodeFrequency(JsonNode codeFrequency, Integer repositoryId) {
        try {
            logger.info("处理代码频率数据，共{}周数据", codeFrequency.size());
            
            for (JsonNode weekData : codeFrequency) {
                if (weekData.isArray() && weekData.size() >= 3) {
                    GitActivity activity = new GitActivity();
                    activity.setRepositoryId(repositoryId.longValue());
                    activity.setActivityType("code_frequency");
                    
                    // 提取周时间戳
                    long timestamp = weekData.get(0).asLong();
                    if (timestamp > 0) {
                        activity.setActivityTime(LocalDateTime.ofEpochSecond(timestamp, 0, java.time.ZoneOffset.UTC));
                    } else {
                        activity.setActivityTime(LocalDateTime.now());
                    }
                    
                    // 提取代码增删数据
                    int additions = weekData.get(1).asInt();
                    int deletions = Math.abs(weekData.get(2).asInt()); // 删除数通常为负数，取绝对值
                    
                    activity.setLinesAdded(additions);
                    activity.setLinesDeleted(deletions);
                    
                    // 保存元数据
                    String metadata = String.format("{\"week\":%d,\"additions\":%d,\"deletions\":%d}", 
                        timestamp, additions, deletions);
                    activity.setMetadata(metadata);
                    
                    activity.setCreatedAt(LocalDateTime.now());
                    gitActivityMapper.insert(activity);
                }
            }
            
            logger.info("代码频率数据处理完成");
        } catch (Exception e) {
            logger.error("处理代码频率数据失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理参与度数据
     */
    private void processParticipation(JsonNode participation, Integer repositoryId) {
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        // 清理旧的参与度数据
        try {
            gitActivityMapper.deleteByRepoIdAndActivityType(repositoryId.longValue(), "participation");
            logger.info("已清理仓库{}的旧参与度数据", repositoryId);
        } catch (Exception e) {
            logger.warn("清理旧参与度数据失败: {}", e.getMessage());
        }
        
        while (retryCount < maxRetries) {
            try {
                logger.info("处理参与度统计数据 (尝试 {}/{})", retryCount + 1, maxRetries);
                
                GitActivity activity = new GitActivity();
                activity.setRepositoryId(repositoryId.longValue());
                activity.setActivityType("participation");
                activity.setActivityTime(LocalDateTime.now());
                
                // 提取所有者和其他人的提交数据
                JsonNode all = participation.get("all");
                JsonNode owner = participation.get("owner");
                
                if (all != null && all.isArray() && owner != null && owner.isArray()) {
                    // 计算总提交数
                    int totalCommits = 0;
                    int ownerCommits = 0;
                    
                    for (JsonNode weekCommits : all) {
                        totalCommits += weekCommits.asInt();
                    }
                    
                    for (JsonNode weekCommits : owner) {
                        ownerCommits += weekCommits.asInt();
                    }
                    
                    int otherCommits = totalCommits - ownerCommits;
                    
                    // 保存统计数据
                    activity.setTotalCommits(totalCommits);
                    activity.setOwnerCommits(ownerCommits);
                    activity.setOtherCommits(otherCommits);
                    
                    // 保存元数据
                    String metadata = String.format("{\"total\":%d,\"owner\":%d,\"others\":%d,\"all_weeks\":%d,\"owner_weeks\":%d}", 
                        totalCommits, ownerCommits, otherCommits, all.size(), owner.size());
                    activity.setMetadata(metadata);
                    
                    saveActivity(activity);
                    logger.info("参与度数据处理完成: 总提交{}, 所有者{}, 其他人{}", totalCommits, ownerCommits, otherCommits);
                    return; // 成功处理，退出重试循环
                } else {
                    throw new RuntimeException("参与度数据格式不正确: all或owner字段缺失或格式错误");
                }
                
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                logger.warn("处理参与度数据失败 (尝试 {}/{}): {}", retryCount, maxRetries, e.getMessage());
                
                if (retryCount < maxRetries) {
                    try {
                        // 递增等待时间：1秒、2秒、3秒
                        Thread.sleep(retryCount * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("处理参与度数据时被中断", ie);
                    }
                }
            }
        }
        
        // 所有重试都失败了
        logger.error("处理参与度数据最终失败，已重试{}次", maxRetries);
        throw new RuntimeException("处理参与度数据失败: " + (lastException != null ? lastException.getMessage() : "未知错误"), lastException);
    }
    
    /**
     * 处理Pull Request数据
     */
    private void processPullRequests(JsonNode pullRequests, Integer repositoryId) {
        try {
            logger.info("处理Pull Request数据，共{}个PR", pullRequests.size());
            
            for (JsonNode pr : pullRequests) {
                GitActivity activity = new GitActivity();
                activity.setRepositoryId(repositoryId.longValue());
                activity.setActivityType("pull_request");
                
                // 提取PR基本信息
                String title = getTextValue(pr, "title");
                String state = getTextValue(pr, "state");
                int number = getIntValue(pr, "number");
                String createdAt = getTextValue(pr, "created_at");
                String updatedAt = getTextValue(pr, "updated_at");
                String mergedAt = getTextValue(pr, "merged_at");
                
                // 设置PR相关字段
                activity.setPrNumber(number);
                activity.setPrState(state);
                activity.setPrTitle(title);
                
                // 解析创建时间
                if (createdAt != null) {
                    activity.setActivityTime(parseGitHubDateTime(createdAt));
                } else {
                    activity.setActivityTime(LocalDateTime.now());
                }
                
                // 提取作者信息
                JsonNode user = pr.get("user");
                if (user != null) {
                    String githubUsername = getTextValue(user, "login");
                    if (StringUtils.hasText(githubUsername)) {
                        Integer memberId = findMemberIdByGithubUsername(githubUsername);
                        if (memberId != null) {
                            activity.setMemberId(memberId.longValue());
                            logger.debug("Pull Request已关联到系统成员: {} -> 成员ID: {}", githubUsername, memberId);
                        } else {
                            logger.info("Pull Request保存为未关联用户: GitHub用户={}, PR编号={}", githubUsername, number);
                        }
                        activity.setAuthorName(githubUsername);
                    }
                    activity.setAuthorEmail(getTextValue(user, "email"));
                }
                
                // 构建元数据
                String metadata = String.format("{\"title\":\"%s\",\"state\":\"%s\",\"number\":%d,\"created_at\":\"%s\",\"updated_at\":\"%s\",\"merged_at\":\"%s\"}", 
                    title != null ? title.replace("\"", "\\\"") : "", 
                    state != null ? state : "", 
                    number, 
                    createdAt != null ? createdAt : "", 
                    updatedAt != null ? updatedAt : "", 
                    mergedAt != null ? mergedAt : "");
                activity.setMetadata(metadata);
                
                saveActivity(activity);
            }
            
            logger.info("Pull Request数据处理完成");
        } catch (Exception e) {
            logger.error("处理Pull Request数据失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理Issues数据
     */
    private void processIssues(JsonNode issues, Integer repositoryId) {
        try {
            logger.info("处理Issues数据，共{}个Issue", issues.size());
            
            for (JsonNode issue : issues) {
                // 跳过Pull Request (GitHub API中PR也会出现在Issues中)
                if (issue.has("pull_request")) {
                    continue;
                }
                
                GitActivity activity = new GitActivity();
                activity.setRepositoryId(repositoryId.longValue());
                activity.setActivityType("issue");
                
                // 提取Issue基本信息
                String title = getTextValue(issue, "title");
                String state = getTextValue(issue, "state");
                int number = getIntValue(issue, "number");
                String createdAt = getTextValue(issue, "created_at");
                String updatedAt = getTextValue(issue, "updated_at");
                String closedAt = getTextValue(issue, "closed_at");
                
                // 设置Issue相关字段
                activity.setIssueNumber(number);
                activity.setIssueState(state);
                activity.setIssueTitle(title);
                
                // 解析创建时间
                if (createdAt != null) {
                    activity.setActivityTime(parseGitHubDateTime(createdAt));
                } else {
                    activity.setActivityTime(LocalDateTime.now());
                }
                
                // 提取作者信息
                JsonNode user = issue.get("user");
                if (user != null) {
                    String githubUsername = getTextValue(user, "login");
                    if (StringUtils.hasText(githubUsername)) {
                        Integer memberId = findMemberIdByGithubUsername(githubUsername);
                        if (memberId != null) {
                            activity.setMemberId(memberId.longValue());
                            logger.debug("Issue已关联到系统成员: {} -> 成员ID: {}", githubUsername, memberId);
                        } else {
                            logger.info("Issue保存为未关联用户: GitHub用户={}, Issue编号={}", githubUsername, number);
                        }
                        activity.setAuthorName(githubUsername);
                    }
                    activity.setAuthorEmail(getTextValue(user, "email"));
                }
                
                // 提取标签信息
                JsonNode labels = issue.get("labels");
                StringBuilder labelNames = new StringBuilder();
                if (labels != null && labels.isArray()) {
                    for (JsonNode label : labels) {
                        if (labelNames.length() > 0) {
                            labelNames.append(",");
                        }
                        labelNames.append(getTextValue(label, "name"));
                    }
                }
                
                // 构建元数据
                String metadata = String.format("{\"title\":\"%s\",\"state\":\"%s\",\"number\":%d,\"created_at\":\"%s\",\"updated_at\":\"%s\",\"closed_at\":\"%s\",\"labels\":\"%s\"}", 
                    title != null ? title.replace("\"", "\\\"") : "", 
                    state != null ? state : "", 
                    number, 
                    createdAt != null ? createdAt : "", 
                    updatedAt != null ? updatedAt : "", 
                    closedAt != null ? closedAt : "", 
                    labelNames.toString());
                activity.setMetadata(metadata);
                
                saveActivity(activity);
            }
            
            logger.info("Issues数据处理完成");
        } catch (Exception e) {
            logger.error("处理Issues数据失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理Releases数据
     */
    private void processReleases(JsonNode releases, Integer repositoryId) {
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        // 清理旧的Release数据
        try {
            gitActivityMapper.deleteByRepoIdAndActivityType(repositoryId.longValue(), "release");
            logger.info("已清理仓库{}的旧Release数据", repositoryId);
        } catch (Exception e) {
            logger.warn("清理旧Release数据失败: {}", e.getMessage());
        }
        
        while (retryCount < maxRetries) {
            try {
                logger.info("处理Releases数据，共{}个Release (尝试 {}/{})", releases.size(), retryCount + 1, maxRetries);
                
                for (JsonNode release : releases) {
                    GitActivity activity = new GitActivity();
                    activity.setRepositoryId(repositoryId.longValue());
                    activity.setActivityType("release");
                    
                    // 提取Release基本信息
                    String tagName = getTextValue(release, "tag_name");
                    String name = getTextValue(release, "name");
                    String publishedAt = getTextValue(release, "published_at");
                    String createdAt = getTextValue(release, "created_at");
                    boolean prerelease = release.path("prerelease").asBoolean();
                    boolean draft = release.path("draft").asBoolean();
                    
                    // 设置Release相关字段
                    activity.setReleaseTag(tagName);
                    activity.setReleaseName(name);
                    
                    // 解析发布时间
                    if (publishedAt != null) {
                        activity.setActivityTime(parseGitHubDateTime(publishedAt));
                    } else if (createdAt != null) {
                        activity.setActivityTime(parseGitHubDateTime(createdAt));
                    } else {
                        activity.setActivityTime(LocalDateTime.now());
                    }
                    
                    // 提取作者信息
                    JsonNode author = release.get("author");
                    if (author != null) {
                        String githubUsername = getTextValue(author, "login");
                        if (StringUtils.hasText(githubUsername)) {
                            Integer memberId = findMemberIdByGithubUsername(githubUsername);
                            if (memberId != null) {
                                activity.setMemberId(memberId.longValue());
                                logger.debug("Release已关联到系统成员: {} -> 成员ID: {}", githubUsername, memberId);
                            } else {
                                logger.info("Release保存为未关联用户: GitHub用户={}, Release标签={}", githubUsername, tagName);
                            }
                            activity.setAuthorName(githubUsername);
                        }
                        activity.setAuthorEmail(getTextValue(author, "email"));
                    }
                    
                    // 构建元数据
                    String metadata = String.format("{\"tag_name\":\"%s\",\"name\":\"%s\",\"published_at\":\"%s\",\"created_at\":\"%s\",\"prerelease\":%b,\"draft\":%b}", 
                        tagName != null ? tagName : "", 
                        name != null ? name.replace("\"", "\\\"") : "", 
                        publishedAt != null ? publishedAt : "", 
                        createdAt != null ? createdAt : "", 
                        prerelease, 
                        draft);
                    activity.setMetadata(metadata);
                    
                    saveActivity(activity);
                }
                
                logger.info("Releases数据处理完成");
                return; // 成功处理，退出重试循环
                
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                logger.warn("处理Releases数据失败 (尝试 {}/{}): {}", retryCount, maxRetries, e.getMessage());
                
                if (retryCount < maxRetries) {
                    try {
                        // 递增等待时间：1秒、2秒、3秒
                        Thread.sleep(retryCount * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("处理Releases数据时被中断", ie);
                    }
                }
            }
        }
        
        // 所有重试都失败了
        logger.error("处理Releases数据最终失败，已重试{}次", maxRetries);
        throw new RuntimeException("处理Releases数据失败: " + (lastException != null ? lastException.getMessage() : "未知错误"), lastException);
    }
    
    /**
     * 处理分支数据
     */
    private void processBranches(JsonNode branches, Integer repositoryId) {
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        // 清理旧的分支数据
        try {
            gitActivityMapper.deleteByRepoIdAndActivityType(repositoryId.longValue(), "branch");
            logger.info("已清理仓库{}的旧分支数据", repositoryId);
        } catch (Exception e) {
            logger.warn("清理旧分支数据失败: {}", e.getMessage());
        }
        
        while (retryCount < maxRetries) {
            try {
                logger.info("处理分支数据，共{}个分支 (尝试 {}/{})", branches.size(), retryCount + 1, maxRetries);
                
                for (JsonNode branch : branches) {
                    GitActivity activity = new GitActivity();
                    activity.setRepositoryId(repositoryId.longValue());
                    activity.setActivityType("branch");
                    
                    String name = getTextValue(branch, "name");
                    String sha = getTextValue(branch.path("commit"), "sha");
                    
                    // 构建元数据
                    String metadata = String.format("{\"name\":\"%s\",\"sha\":\"%s\"}", name, sha);
                    activity.setMetadata(metadata);
                    activity.setActivityTime(LocalDateTime.now());
                    activity.setCreatedAt(LocalDateTime.now());
                    
                    gitActivityMapper.insert(activity);
                }
                
                logger.info("分支数据处理完成");
                return; // 成功处理，退出重试循环
                
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                logger.warn("处理分支数据失败 (尝试 {}/{}): {}", retryCount, maxRetries, e.getMessage());
                
                if (retryCount < maxRetries) {
                    try {
                        // 递增等待时间：1秒、2秒、3秒
                        Thread.sleep(retryCount * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("处理分支数据时被中断", ie);
                    }
                }
            }
        }
        
        // 所有重试都失败了
        logger.error("处理分支数据最终失败，已重试{}次", maxRetries);
        throw new RuntimeException("处理分支数据失败: " + (lastException != null ? lastException.getMessage() : "未知错误"), lastException);
    }
    
    /**
     * 处理标签数据
     */
    private void processTags(JsonNode tags, Integer repositoryId) {
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        // 清理旧的标签数据
        try {
            gitActivityMapper.deleteByRepoIdAndActivityType(repositoryId.longValue(), "tag_activity");
            logger.info("已清理仓库{}的旧标签数据", repositoryId);
        } catch (Exception e) {
            logger.warn("清理旧标签数据失败: {}", e.getMessage());
        }
        
        while (retryCount < maxRetries) {
            try {
                logger.info("处理标签数据，共{}个标签 (尝试 {}/{})", tags.size(), retryCount + 1, maxRetries);
                
                for (JsonNode tag : tags) {
                    // 提取标签基本信息
                    String tagName = tag.has("name") ? tag.get("name").asText() : "unknown";
                    String sha = null;
                    if (tag.has("commit") && tag.get("commit").has("sha")) {
                        sha = tag.get("commit").get("sha").asText();
                    }
                    
                    // 提取标签URL
                    String tagUrl = tag.has("zipball_url") ? tag.get("zipball_url").asText() : null;
                    
                    // 构建元数据
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("tag_name", tagName);
                    if (sha != null) {
                        metadata.put("commit_sha", sha);
                    }
                    if (tagUrl != null) {
                        metadata.put("zipball_url", tagUrl);
                    }
                    metadata.put("data_type", "tag");
                    
                    // 保存活动记录
                    saveActivity(repositoryId, "tag_activity", metadata, null, null);
                }
                
                logger.info("标签数据处理完成");
                return; // 成功处理，退出重试循环
                
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                logger.warn("处理标签数据失败 (尝试 {}/{}): {}", retryCount, maxRetries, e.getMessage());
                
                if (retryCount < maxRetries) {
                    try {
                        // 递增等待时间：1秒、2秒、3秒
                        Thread.sleep(retryCount * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("处理标签数据时被中断", ie);
                    }
                }
            }
        }
        
        // 所有重试都失败了
        logger.error("处理标签数据最终失败，已重试{}次", maxRetries);
        throw new RuntimeException("处理标签数据失败: " + (lastException != null ? lastException.getMessage() : "未知错误"), lastException);
    }
    
    /**
     * 解析GitHub日期时间格式
     * @param dateTimeStr GitHub日期时间字符串
     * @return LocalDateTime
     */
    private LocalDateTime parseGitHubDateTime(String dateTimeStr) {
        try {
            // GitHub API返回的时间格式: 2023-01-01T12:00:00Z
            return LocalDateTime.parse(dateTimeStr.replace("Z", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            logger.warn("解析GitHub时间失败: {}", dateTimeStr);
            return LocalDateTime.now();
        }
    }
    
    /**
     * 安全获取JSON节点的文本值
     * @param node JSON节点
     * @param fieldName 字段名
     * @return 文本值
     */
    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null ? field.asText() : null;
    }
    
    /**
     * 安全获取JSON节点的整数值
     * @param node JSON节点
     * @param fieldName 字段名
     * @return 整数值
     */
    private Integer getIntValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null ? field.asInt() : 0;
    }
    
    /**
     * 保存单个活动记录（带去重检查）
     * @param activity Git活动记录
     */
    private void saveActivity(GitActivity activity) {
        try {
            // 去重检查
            if (isDuplicateActivity(activity)) {
                logger.debug("跳过重复的活动记录: 仓库ID={}, 类型={}", activity.getRepositoryId(), activity.getActivityType());
                return;
            }
            
            activity.setCreatedAt(LocalDateTime.now());
            gitActivityMapper.insert(activity);
            logger.debug("成功保存活动记录: 仓库ID={}, 类型={}", activity.getRepositoryId(), activity.getActivityType());
        } catch (Exception e) {
            logger.warn("保存活动记录失败: {}", e.getMessage());
        }
    }
    
    /**
     * 保存活动记录（重载方法，带去重检查）
     * @param repositoryId 仓库ID
     * @param activityType 活动类型
     * @param metadata 元数据
     * @param authorName 作者名称
     * @param authorEmail 作者邮箱
     */
    private void saveActivity(Integer repositoryId, String activityType, Map<String, Object> metadata, String authorName, String authorEmail) {
        try {
            GitActivity activity = new GitActivity();
            activity.setRepositoryId(repositoryId.longValue());
            activity.setActivityType(activityType);
            activity.setActivityTime(LocalDateTime.now());
            activity.setCreatedAt(LocalDateTime.now());
            
            if (authorName != null) {
                // 尝试通过GitHub用户名查找member_id
                if (StringUtils.hasText(authorName)) {
                    Integer memberId = findMemberIdByGithubUsername(authorName);
                    if (memberId != null) {
                        activity.setMemberId(memberId.longValue());
                    }
                }
                activity.setAuthorName(authorName);
            }
            if (authorEmail != null) {
                activity.setAuthorEmail(authorEmail);
            }
            
            // 将Map转换为JSON字符串
            if (metadata != null) {
                StringBuilder metadataJson = new StringBuilder("{");
                boolean first = true;
                for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                    if (!first) {
                        metadataJson.append(",");
                    }
                    metadataJson.append("\"").append(entry.getKey()).append("\":");
                    if (entry.getValue() instanceof String) {
                        metadataJson.append("\"").append(entry.getValue()).append("\"");
                    } else {
                        metadataJson.append(entry.getValue());
                    }
                    first = false;
                }
                metadataJson.append("}");
                activity.setMetadata(metadataJson.toString());
            }
            
            // 去重检查
            if (isDuplicateActivity(activity)) {
                logger.debug("跳过重复的活动记录: 仓库ID={}, 类型={}", repositoryId, activityType);
                return;
            }
            
            gitActivityMapper.insert(activity);
            logger.debug("成功保存活动记录: 仓库ID={}, 类型={}", repositoryId, activityType);
        } catch (Exception e) {
            logger.warn("保存活动记录失败: {}", e.getMessage());
        }
    }
    
    /**
     * 检查是否为重复的活动记录
     * @param activity 活动记录
     * @return 是否重复
     */
    private boolean isDuplicateActivity(GitActivity activity) {
        try {
            // 对于commit类型，使用commit_hash进行去重
            if ("commit".equals(activity.getActivityType()) && StringUtils.hasText(activity.getCommitHash())) {
                Long count = gitActivityMapper.countByRepoIdAndCommitHash(activity.getRepositoryId(), activity.getCommitHash());
                return count != null && count > 0;
            }
            
            // 对于其他类型，使用metadata进行去重
            if (StringUtils.hasText(activity.getMetadata())) {
                Long count = gitActivityMapper.countByRepoIdAndTypeAndMetadata(
                    activity.getRepositoryId(), 
                    activity.getActivityType(), 
                    activity.getMetadata()
                );
                return count != null && count > 0;
            }
            
            return false;
        } catch (Exception e) {
            logger.warn("检查重复记录失败: {}", e.getMessage());
            return false;
        }
    }
}