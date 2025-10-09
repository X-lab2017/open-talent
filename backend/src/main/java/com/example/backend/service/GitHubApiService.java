package com.example.backend.service;

import com.example.backend.config.GitHubConfig;
import com.example.backend.exception.GitHubApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * GitHub API服务类
 */
@Service
public class GitHubApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(GitHubApiService.class);
    
    @Autowired
    private GitHubConfig gitHubConfig;
    
    @Autowired
    private GitHubConfigService gitHubConfigService;
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public GitHubApiService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 获取仓库信息
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param courseId 课程ID（用于获取对应的GitHub Token）
     * @return 仓库信息
     */
    public JsonNode getRepositoryInfo(String owner, String repo, Integer courseId) {
        try {
            logger.info("获取GitHub仓库信息: {}/{}, 课程ID: {}", owner, repo, courseId);
            String url = gitHubConfig.getBaseUrl() + "/repos/" + owner + "/" + repo;
            JsonNode result = makeApiRequest(url, courseId);
            logger.debug("成功获取仓库信息: {}/{}", owner, repo);
            return result;
        } catch (GitHubApiException e) {
            logger.error("获取GitHub仓库信息失败: {}/{}, 错误: {}", owner, repo, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("获取GitHub仓库信息异常: {}/{}, 错误: {}", owner, repo, e.getMessage(), e);
            throw new GitHubApiException("获取GitHub仓库信息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取仓库信息（兼容旧版本，使用默认Token）
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @return 仓库信息
     */
    public JsonNode getRepositoryInfo(String owner, String repo) {
        return getRepositoryInfo(owner, repo, null);
    }
    
    /**
     * 获取仓库的提交记录
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param since 开始时间
     * @param until 结束时间
     * @param courseId 课程ID（用于获取对应的GitHub Token）
     * @return 提交记录列表
     */
    public List<JsonNode> getCommits(String owner, String repo, LocalDateTime since, LocalDateTime until, Integer courseId) {
        try {
            logger.info("获取GitHub提交记录: {}/{}, 课程ID: {}", owner, repo, courseId);
            
            StringBuilder url = new StringBuilder(gitHubConfig.getBaseUrl())
                    .append("/repos/").append(owner).append("/").append(repo).append("/commits")
                    .append("?per_page=").append(gitHubConfig.getPerPage());
            
            if (since != null) {
                url.append("&since=").append(since.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("Z");
            }
            if (until != null) {
                url.append("&until=").append(until.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("Z");
            }
            
            JsonNode response = makeApiRequest(url.toString(), courseId);
            
            List<JsonNode> commits = new ArrayList<>();
            if (response.isArray()) {
                for (JsonNode commit : response) {
                    commits.add(commit);
                }
            }
            
            logger.info("成功获取{}条提交记录: {}/{}", commits.size(), owner, repo);
            return commits;
            
        } catch (GitHubApiException e) {
            logger.error("获取GitHub提交记录失败: {}/{}, 错误: {}", owner, repo, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("获取GitHub提交记录异常: {}/{}, 错误: {}", owner, repo, e.getMessage(), e);
            throw new GitHubApiException("获取GitHub提交记录失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取仓库的提交记录（兼容旧版本，使用默认Token）
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param since 开始时间
     * @param until 结束时间
     * @return 提交记录列表
     */
    public List<JsonNode> getCommits(String owner, String repo, LocalDateTime since, LocalDateTime until) {
        return getCommits(owner, repo, since, until, null);
    }
    
    /**
     * 获取仓库的分支列表
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param courseId 课程ID（用于获取对应的GitHub Token）
     * @return 分支列表
     */
    public List<JsonNode> getBranches(String owner, String repo, Integer courseId) {
        try {
            logger.info("获取GitHub分支列表: {}/{}, 课程ID: {}", owner, repo, courseId);
            String url = gitHubConfig.getBaseUrl() + "/repos/" + owner + "/" + repo + "/branches";
            JsonNode response = makeApiRequest(url, courseId);
            
            List<JsonNode> branches = new ArrayList<>();
            if (response.isArray()) {
                for (JsonNode branch : response) {
                    branches.add(branch);
                }
            }
            
            logger.info("成功获取{}个分支: {}/{}", branches.size(), owner, repo);
            return branches;
            
        } catch (GitHubApiException e) {
            logger.error("获取GitHub分支列表失败: {}/{}, 错误: {}", owner, repo, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("获取GitHub分支列表异常: {}/{}, 错误: {}", owner, repo, e.getMessage(), e);
            throw new GitHubApiException("获取GitHub分支列表失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取仓库的分支列表（兼容旧版本，使用默认Token）
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @return 分支列表
     */
    public List<JsonNode> getBranches(String owner, String repo) {
        return getBranches(owner, repo, null);
    }
    
    /**
     * 获取仓库的贡献者列表
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @return 贡献者列表
     */
    public List<JsonNode> getContributors(String owner, String repo) {
        try {
            logger.info("获取GitHub贡献者列表: {}/{}", owner, repo);
            String url = gitHubConfig.getBaseUrl() + "/repos/" + owner + "/" + repo + "/contributors";
            JsonNode response = makeApiRequest(url);
            
            List<JsonNode> contributors = new ArrayList<>();
            if (response.isArray()) {
                for (JsonNode contributor : response) {
                    contributors.add(contributor);
                }
            }
            
            logger.info("成功获取{}个贡献者: {}/{}", contributors.size(), owner, repo);
            return contributors;
            
        } catch (GitHubApiException e) {
            logger.error("获取GitHub贡献者列表失败: {}/{}, 错误: {}", owner, repo, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("获取GitHub贡献者列表异常: {}/{}, 错误: {}", owner, repo, e.getMessage(), e);
            throw new GitHubApiException("获取GitHub贡献者列表失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行GitHub API请求
     * @param url API URL
     * @param courseId 课程ID（用于获取对应的GitHub Token）
     * @return 响应的JSON数据
     * @throws GitHubApiException 请求异常
     */
    private JsonNode makeApiRequest(String url, Integer courseId) throws GitHubApiException {
        logger.debug("执行GitHub API请求: {}, 课程ID: {}", url, courseId);
        
        Request.Builder requestBuilder = new Request.Builder().url(url);
        
        // 添加认证头
        String token = getTokenForCourse(courseId);
        if (StringUtils.hasText(token)) {
            requestBuilder.addHeader("Authorization", "token " + token);
            logger.debug("使用课程{}的GitHub Token进行认证", courseId);
        } else {
            logger.warn("未配置GitHub Token（课程ID: {}），API请求可能受到限流影响", courseId);
        }
        
        requestBuilder.addHeader("Accept", "application/vnd.github.v3+json");
        requestBuilder.addHeader("User-Agent", "OpenTalent-Backend");
        
        Request request = requestBuilder.build();
        
        return executeRequest(request, url);
    }
    
    /**
     * 执行GitHub API请求（兼容旧版本，使用默认Token）
     * @param url API URL
     * @return 响应的JSON数据
     * @throws GitHubApiException 请求异常
     */
    private JsonNode makeApiRequest(String url) throws GitHubApiException {
        return makeApiRequest(url, null);
    }
    
    /**
     * 获取指定课程的GitHub Token
     * @param courseId 课程ID
     * @return GitHub Token
     */
    private String getTokenForCourse(Integer courseId) {
        if (courseId != null) {
            try {
                // 将Integer转换为Long类型，以匹配GitHubConfigService的方法签名
                Long courseIdLong = courseId.longValue();
                return gitHubConfigService.getTokenByCourseId(courseIdLong);
            } catch (Exception e) {
                logger.warn("获取课程{}的GitHub Token失败: {}", courseId, e.getMessage());
            }
        }
        
        // 回退到默认配置的Token
        return gitHubConfig.getToken();
    }
    
    /**
     * 执行HTTP请求
     * @param request HTTP请求
     * @param url 请求URL
     * @return 响应的JSON数据
     * @throws GitHubApiException 请求异常
     */
    private JsonNode executeRequest(Request request, String url) throws GitHubApiException {
        return executeRequestWithRetry(request, url, 0);
    }
    
    /**
     * 执行HTTP请求（带重试机制）
     * @param request HTTP请求
     * @param url 请求URL
     * @param retryCount 当前重试次数
     * @return 响应的JSON数据
     * @throws GitHubApiException 请求异常
     */
    private JsonNode executeRequestWithRetry(Request request, String url, int retryCount) throws GitHubApiException {
        final int MAX_RETRIES = 3;
        final long RETRY_DELAY_MS = 2000; // 2秒延迟
        
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            
            // 处理202状态码（GitHub Statistics API特殊情况）
            if (response.code() == 202) {
                if (retryCount < MAX_RETRIES) {
                    logger.info("GitHub Statistics API返回202，数据正在计算中，{}ms后重试 (第{}/{}次): {}", 
                               RETRY_DELAY_MS, retryCount + 1, MAX_RETRIES, url);
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new GitHubApiException("请求被中断", e);
                    }
                    return executeRequestWithRetry(request, url, retryCount + 1);
                } else {
                    logger.warn("GitHub Statistics API在{}次重试后仍返回202，跳过此次同步: {}", MAX_RETRIES, url);
                    throw new GitHubApiException("GitHub Statistics API数据计算超时，请稍后重试", 202, responseBody);
                }
            }
            
            if (!response.isSuccessful()) {
                logger.error("GitHub API请求失败: {} - {}, 响应: {}", response.code(), response.message(), responseBody);
                
                // 处理特定的HTTP状态码
                switch (response.code()) {
                    case 401:
                        throw new GitHubApiException("GitHub API认证失败，请检查Token配置", response.code(), responseBody);
                    case 403:
                        if (responseBody.contains("rate limit")) {
                            throw new GitHubApiException("GitHub API请求频率超限，请稍后重试", response.code(), responseBody);
                        } else {
                            throw new GitHubApiException("GitHub API访问被拒绝", response.code(), responseBody);
                        }
                    case 404:
                        throw new GitHubApiException("GitHub仓库或资源不存在", response.code(), responseBody);
                    default:
                        throw new GitHubApiException("GitHub API请求失败: " + response.code() + " - " + response.message(), response.code(), responseBody);
                }
            }
            
            logger.debug("GitHub API请求成功: {}", url);
            return objectMapper.readTree(responseBody);
            
        } catch (IOException e) {
            logger.error("GitHub API请求IO异常: {}", e.getMessage(), e);
            throw new GitHubApiException("GitHub API请求网络异常: " + e.getMessage(), e);
        } catch (GitHubApiException e) {
            // 重新抛出GitHubApiException
            throw e;
        } catch (Exception e) {
            logger.error("GitHub API请求未知异常: {}", e.getMessage(), e);
            throw new GitHubApiException("GitHub API请求处理异常: " + e.getMessage(), e);
        }
    }
    

    
    /**
     * 获取仓库的贡献者统计数据
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param courseId 课程ID（用于获取对应的GitHub Token）
     * @return 贡献者统计数据
     */
    public JsonNode getContributorStatistics(String owner, String repo, Integer courseId) {
        try {
            logger.info("获取GitHub贡献者统计: {}/{}, 课程ID: {}", owner, repo, courseId);
            String url = gitHubConfig.getBaseUrl() + "/repos/" + owner + "/" + repo + "/stats/contributors";
            return makeApiRequest(url, courseId);
        } catch (GitHubApiException e) {
            logger.error("获取GitHub贡献者统计失败: {}/{}, 错误: {}", owner, repo, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("获取GitHub贡献者统计异常: {}/{}, 错误: {}", owner, repo, e.getMessage(), e);
            throw new GitHubApiException("获取GitHub贡献者统计失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取仓库的贡献者统计数据（兼容旧版本，使用默认Token）
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @return 贡献者统计数据
     */
    public JsonNode getContributorStatistics(String owner, String repo) {
        return getContributorStatistics(owner, repo, null);
    }

    /**
     * 获取仓库的提交活动统计
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param courseId 课程ID（用于获取对应的GitHub Token）
     * @return 提交活动统计数据
     */
    public JsonNode getCommitActivity(String owner, String repo, Integer courseId) {
        try {
            logger.info("获取GitHub提交活动统计: {}/{}, 课程ID: {}", owner, repo, courseId);
            String url = gitHubConfig.getBaseUrl() + "/repos/" + owner + "/" + repo + "/stats/commit_activity";
            return makeApiRequest(url, courseId);
        } catch (GitHubApiException e) {
            logger.error("获取GitHub提交活动统计失败: {}/{}, 错误: {}", owner, repo, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("获取GitHub提交活动统计异常: {}/{}, 错误: {}", owner, repo, e.getMessage(), e);
            throw new GitHubApiException("获取GitHub提交活动统计失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取仓库的提交活动统计（兼容旧版本，使用默认Token）
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @return 提交活动统计数据
     */
    public JsonNode getCommitActivity(String owner, String repo) {
        return getCommitActivity(owner, repo, null);
    }

    /**
     * 获取仓库的代码频率统计
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param courseId 课程ID（用于获取对应的GitHub Token）
     * @return 代码频率统计数据
     */
    public JsonNode getCodeFrequency(String owner, String repo, Integer courseId) {
        try {
            logger.info("获取GitHub代码频率统计: {}/{}, 课程ID: {}", owner, repo, courseId);
            String url = gitHubConfig.getBaseUrl() + "/repos/" + owner + "/" + repo + "/stats/code_frequency";
            return makeApiRequest(url, courseId);
        } catch (GitHubApiException e) {
            logger.error("获取GitHub代码频率统计失败: {}/{}, 错误: {}", owner, repo, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("获取GitHub代码频率统计异常: {}/{}, 错误: {}", owner, repo, e.getMessage(), e);
            throw new GitHubApiException("获取GitHub代码频率统计失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取仓库的代码频率统计（兼容旧版本，使用默认Token）
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @return 代码频率统计数据
     */
    public JsonNode getCodeFrequency(String owner, String repo) {
        return getCodeFrequency(owner, repo, null);
    }

    /**
     * 获取仓库的参与度统计
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param courseId 课程ID（用于获取对应的GitHub Token）
     * @return 参与度统计数据
     */
    public JsonNode getParticipation(String owner, String repo, Integer courseId) {
        try {
            logger.info("获取GitHub参与度统计: {}/{}, 课程ID: {}", owner, repo, courseId);
            String url = gitHubConfig.getBaseUrl() + "/repos/" + owner + "/" + repo + "/stats/participation";
            return makeApiRequest(url, courseId);
        } catch (GitHubApiException e) {
            logger.error("获取GitHub参与度统计失败: {}/{}, 错误: {}", owner, repo, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("获取GitHub参与度统计异常: {}/{}, 错误: {}", owner, repo, e.getMessage(), e);
            throw new GitHubApiException("获取GitHub参与度统计失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取仓库的参与度统计（兼容旧版本，使用默认Token）
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @return 参与度统计数据
     */
    public JsonNode getParticipation(String owner, String repo) {
        return getParticipation(owner, repo, null);
    }

    /**
     * 获取仓库的Pull Request列表
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param state PR状态 (open, closed, all)
     * @param courseId 课程ID（用于获取对应的GitHub Token）
     * @return Pull Request列表
     */
    public List<JsonNode> getPullRequests(String owner, String repo, String state, Integer courseId) {
        try {
            logger.info("获取GitHub Pull Request列表: {}/{}, 状态: {}, 课程ID: {}", owner, repo, state, courseId);
            String url = gitHubConfig.getBaseUrl() + "/repos/" + owner + "/" + repo + "/pulls?state=" + state + "&per_page=" + gitHubConfig.getPerPage();
            JsonNode response = makeApiRequest(url, courseId);
            
            List<JsonNode> pullRequests = new ArrayList<>();
            if (response.isArray()) {
                for (JsonNode pr : response) {
                    pullRequests.add(pr);
                }
            }
            
            logger.info("成功获取{}个Pull Request: {}/{}", pullRequests.size(), owner, repo);
            return pullRequests;
        } catch (GitHubApiException e) {
            logger.error("获取GitHub Pull Request列表失败: {}/{}, 错误: {}", owner, repo, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("获取GitHub Pull Request列表异常: {}/{}, 错误: {}", owner, repo, e.getMessage(), e);
            throw new GitHubApiException("获取GitHub Pull Request列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取仓库的Pull Request列表（兼容旧版本，使用默认Token）
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param state PR状态 (open, closed, all)
     * @return Pull Request列表
     */
    public List<JsonNode> getPullRequests(String owner, String repo, String state) {
        return getPullRequests(owner, repo, state, null);
    }

    /**
     * 获取仓库的Issues列表
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param state Issue状态 (open, closed, all)
     * @param courseId 课程ID（用于获取对应的GitHub Token）
     * @return Issues列表
     */
    public List<JsonNode> getIssues(String owner, String repo, String state, Integer courseId) {
        try {
            logger.info("获取GitHub Issues列表: {}/{}, 状态: {}, 课程ID: {}", owner, repo, state, courseId);
            String url = gitHubConfig.getBaseUrl() + "/repos/" + owner + "/" + repo + "/issues?state=" + state + "&per_page=" + gitHubConfig.getPerPage();
            JsonNode response = makeApiRequest(url, courseId);
            
            List<JsonNode> issues = new ArrayList<>();
            if (response.isArray()) {
                for (JsonNode issue : response) {
                    // 过滤掉Pull Request（GitHub API中PR也会出现在issues中）
                    if (!issue.has("pull_request")) {
                        issues.add(issue);
                    }
                }
            }
            
            logger.info("成功获取{}个Issues: {}/{}", issues.size(), owner, repo);
            return issues;
        } catch (GitHubApiException e) {
            logger.error("获取GitHub Issues列表失败: {}/{}, 错误: {}", owner, repo, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("获取GitHub Issues列表异常: {}/{}, 错误: {}", owner, repo, e.getMessage(), e);
            throw new GitHubApiException("获取GitHub Issues列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取仓库的Issues列表（兼容旧版本，使用默认Token）
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param state Issue状态 (open, closed, all)
     * @return Issues列表
     */
    public List<JsonNode> getIssues(String owner, String repo, String state) {
        return getIssues(owner, repo, state, null);
    }

    /**
     * 获取仓库的Releases列表
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param courseId 课程ID（用于获取对应的GitHub Token）
     * @return Releases列表
     */
    public List<JsonNode> getReleases(String owner, String repo, Integer courseId) {
        try {
            logger.info("获取GitHub Releases列表: {}/{}, 课程ID: {}", owner, repo, courseId);
            String url = gitHubConfig.getBaseUrl() + "/repos/" + owner + "/" + repo + "/releases?per_page=" + gitHubConfig.getPerPage();
            JsonNode response = makeApiRequest(url, courseId);
            
            List<JsonNode> releases = new ArrayList<>();
            if (response.isArray()) {
                for (JsonNode release : response) {
                    releases.add(release);
                }
            }
            
            logger.info("成功获取{}个Releases: {}/{}", releases.size(), owner, repo);
            return releases;
        } catch (GitHubApiException e) {
            logger.error("获取GitHub Releases列表失败: {}/{}, 错误: {}", owner, repo, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("获取GitHub Releases列表异常: {}/{}, 错误: {}", owner, repo, e.getMessage(), e);
            throw new GitHubApiException("获取GitHub Releases列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取仓库的Releases列表（兼容旧版本，使用默认Token）
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @return Releases列表
     */
    public List<JsonNode> getReleases(String owner, String repo) {
        return getReleases(owner, repo, null);
    }

    /**
     * 获取仓库的标签列表
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param courseId 课程ID（用于获取对应的GitHub Token）
     * @return 标签列表
     */
    public List<JsonNode> getTags(String owner, String repo, Integer courseId) {
        try {
            logger.info("获取GitHub标签列表: {}/{}, 课程ID: {}", owner, repo, courseId);
            String url = gitHubConfig.getBaseUrl() + "/repos/" + owner + "/" + repo + "/tags?per_page=" + gitHubConfig.getPerPage();
            JsonNode response = makeApiRequest(url, courseId);
            
            List<JsonNode> tags = new ArrayList<>();
            if (response.isArray()) {
                for (JsonNode tag : response) {
                    tags.add(tag);
                }
            }
            
            logger.info("成功获取{}个标签: {}/{}", tags.size(), owner, repo);
            return tags;
        } catch (GitHubApiException e) {
            logger.error("获取GitHub标签列表失败: {}/{}, 错误: {}", owner, repo, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("获取GitHub标签列表异常: {}/{}, 错误: {}", owner, repo, e.getMessage(), e);
            throw new GitHubApiException("获取GitHub标签列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取仓库的标签列表（兼容旧版本，使用默认Token）
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @return 标签列表
     */
    public List<JsonNode> getTags(String owner, String repo) {
        return getTags(owner, repo, null);
    }

    /**
     * 从仓库URL解析所有者和仓库名
     * @param repoUrl 仓库URL
     * @return [owner, repo]
     */
    public String[] parseRepoUrl(String repoUrl) {
        if (!StringUtils.hasText(repoUrl)) {
            throw new IllegalArgumentException("仓库URL不能为空");
        }
        
        // 处理GitHub URL格式: https://github.com/owner/repo 或 https://github.com/owner/repo.git
        String cleanUrl = repoUrl.replace(".git", "");
        String[] parts = cleanUrl.split("/");
        
        if (parts.length < 2) {
            throw new IllegalArgumentException("无效的GitHub仓库URL: " + repoUrl);
        }
        
        String owner = parts[parts.length - 2];
        String repo = parts[parts.length - 1];
        
        return new String[]{owner, repo};
    }
}