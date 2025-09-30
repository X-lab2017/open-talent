package com.example.backend.service;

import com.example.backend.entity.GitHubConfig;
import com.example.backend.mapper.GitHubConfigMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * GitHub配置服务类
 */
@Service
public class GitHubConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(GitHubConfigService.class);
    
    @Autowired
    private GitHubConfigMapper gitHubConfigMapper;
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public GitHubConfigService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 保存或更新GitHub配置
     * @param courseId 课程ID
     * @param githubToken GitHub Token
     * @return 保存结果
     */
    @Transactional
    public boolean saveOrUpdateConfig(Long courseId, String githubToken) {
        try {
            GitHubConfig existingConfig = gitHubConfigMapper.selectByCourseId(courseId);
            
            if (existingConfig != null) {
                // 更新现有配置
                existingConfig.setGithubToken(githubToken);
                existingConfig.setTokenStatus("active");
                existingConfig.setUpdatedAt(LocalDateTime.now());
                return gitHubConfigMapper.updateById(existingConfig) > 0;
            } else {
                // 创建新配置
                GitHubConfig newConfig = new GitHubConfig();
                newConfig.setCourseId(courseId);
                newConfig.setGithubToken(githubToken);
                newConfig.setTokenStatus("active");
                newConfig.setCreatedAt(LocalDateTime.now());
                newConfig.setUpdatedAt(LocalDateTime.now());
                return gitHubConfigMapper.insert(newConfig) > 0;
            }
        } catch (Exception e) {
            logger.error("保存GitHub配置失败: courseId={}, error={}", courseId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 根据课程ID获取GitHub配置
     * @param courseId 课程ID
     * @return GitHub配置
     */
    public GitHubConfig getConfigByCourseId(Long courseId) {
        return gitHubConfigMapper.selectByCourseId(courseId);
    }
    
    /**
     * 获取所有GitHub配置
     * @return GitHub配置列表
     */
    public List<GitHubConfig> getAllConfigs() {
        return gitHubConfigMapper.selectAllWithCourseName();
    }
    
    /**
     * 验证GitHub Token的有效性
     * @param githubToken GitHub Token
     * @return 验证结果
     */
    public boolean validateToken(String githubToken) {
        if (!StringUtils.hasText(githubToken)) {
            return false;
        }
        
        try {
            Request request = new Request.Builder()
                    .url("https://api.github.com/user")
                    .addHeader("Authorization", "token " + githubToken)
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .addHeader("User-Agent", "OpenTalent-Backend")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    logger.info("GitHub Token验证成功");
                    return true;
                } else {
                    logger.warn("GitHub Token验证失败: HTTP {}", response.code());
                    return false;
                }
            }
        } catch (IOException e) {
            logger.error("GitHub Token验证异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 验证并更新Token状态
     * @param courseId 课程ID
     * @return 验证结果
     */
    @Transactional
    public boolean validateAndUpdateTokenStatus(Long courseId) {
        GitHubConfig config = gitHubConfigMapper.selectByCourseId(courseId);
        if (config == null || !StringUtils.hasText(config.getGithubToken())) {
            return false;
        }
        
        boolean isValid = validateToken(config.getGithubToken());
        String newStatus = isValid ? "active" : "invalid";
        
        gitHubConfigMapper.updateTokenStatus(courseId, newStatus);
        
        return isValid;
    }
    
    /**
     * 删除GitHub配置
     * @param courseId 课程ID
     * @return 删除结果
     */
    @Transactional
    public boolean deleteConfig(Long courseId) {
        try {
            return gitHubConfigMapper.deleteByCourseId(courseId) > 0;
        } catch (Exception e) {
            logger.error("删除GitHub配置失败: courseId={}, error={}", courseId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 根据课程ID获取GitHub Token
     * @param courseId 课程ID
     * @return GitHub Token
     */
    public String getTokenByCourseId(Long courseId) {
        GitHubConfig config = gitHubConfigMapper.selectByCourseId(courseId);
        if (config != null && "active".equals(config.getTokenStatus())) {
            return config.getGithubToken();
        }
        return null;
    }
    
    /**
     * 检查课程是否已配置有效的GitHub Token
     * @param courseId 课程ID
     * @return 是否已配置
     */
    public boolean hasValidToken(Long courseId) {
        GitHubConfig config = gitHubConfigMapper.selectByCourseId(courseId);
        return config != null && "active".equals(config.getTokenStatus()) && StringUtils.hasText(config.getGithubToken());
    }
}