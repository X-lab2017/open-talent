package com.example.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * GitHub API配置类
 */
@Configuration
@ConfigurationProperties(prefix = "github.api")
public class GitHubConfig {
    
    /**
     * GitHub API基础URL
     */
    private String baseUrl = "https://api.github.com";
    
    /**
     * GitHub访问令牌
     */
    private String token;
    
    /**
     * 请求超时时间（秒）
     */
    private int timeout = 30;
    
    /**
     * 每页返回的最大记录数
     */
    private int perPage = 100;
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public int getPerPage() {
        return perPage;
    }
    
    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }
}