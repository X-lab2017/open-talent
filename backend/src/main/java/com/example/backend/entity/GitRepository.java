package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Git仓库实体类
 */
@Data
@TableName("git_repositories")
public class GitRepository {
    
    /**
     * 仓库ID
     */
    @TableId(value = "repository_id", type = IdType.AUTO)
    private Integer repositoryId;
    
    /**
     * 课程ID
     */
    @TableField("course_id")
    private Integer courseId;
    
    /**
     * 课程名称（非数据库字段，通过JOIN查询获取）
     */
    @TableField(exist = false)
    private String courseName;
    
    /**
     * 仓库名称
     */
    @TableField("repository_name")
    private String repositoryName;
    
    /**
     * 仓库URL
     */
    @TableField("repository_url")
    private String repositoryUrl;
    
    /**
     * 分支名称
     */
    @TableField("branch_name")
    private String branchName;
    
    /**
     * 平台类型：github, gitee, gitlab
     */
    @TableField("platform")
    private String platform;
    
    /**
     * 访问令牌
     */
    @TableField("access_token")
    private String accessToken;
    
    /**
     * 最后同步时间
     */
    @TableField("last_sync_time")
    private LocalDateTime lastSyncTime;
    
    /**
     * 同步状态（pending: 待同步, syncing: 同步中, completed: 已完成, failed: 失败）
     */
    @TableField("sync_status")
    private String syncStatus;
    
    /**
     * 是否激活
     */
    @TableField("is_active")
    private Boolean isActive;
    
    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}