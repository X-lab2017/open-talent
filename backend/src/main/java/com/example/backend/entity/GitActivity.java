package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Git活动记录实体类
 */
@Data
@TableName("git_activities")
public class GitActivity {
    @TableId(value = "activity_id", type = IdType.AUTO)
    private Long activityId;
    
    @TableField("repository_id")
    private Long repositoryId;
    
    @TableField("member_id")
    private Long memberId;
    
    @TableField("activity_type")
    private String activityType;
    
    // 提交相关字段
    @TableField("commit_hash")
    private String commitHash;
    
    @TableField("lines_added")
    private Integer linesAdded;
    
    @TableField("lines_deleted")
    private Integer linesDeleted;
    
    @TableField("commit_message")
    private String commitMessage;
    
    @TableField("files_changed")
    private Integer filesChanged;
    
    // Pull Request相关字段
    @TableField("pr_number")
    private Integer prNumber;
    
    @TableField("pr_title")
    private String prTitle;
    
    @TableField("pr_state")
    private String prState; // open, closed, merged
    
    @TableField("pr_merged_at")
    private LocalDateTime prMergedAt;
    
    // Issue相关字段
    @TableField("issue_number")
    private Integer issueNumber;
    
    @TableField("issue_title")
    private String issueTitle;
    
    @TableField("issue_state")
    private String issueState; // open, closed
    
    @TableField("issue_closed_at")
    private LocalDateTime issueClosedAt;
    
    // Release相关字段
    @TableField("release_tag")
    private String releaseTag;
    
    @TableField("release_name")
    private String releaseName;
    
    @TableField("release_published_at")
    private LocalDateTime releasePublishedAt;
    
    @TableField("is_prerelease")
    private Boolean isPrerelease;
    
    // 分支和标签相关字段
    @TableField("branch_name")
    private String branchName;
    
    @TableField("tag_name")
    private String tagName;
    
    // 统计相关字段
    @TableField("total_commits")
    private Integer totalCommits;
    
    @TableField("owner_commits")
    private Integer ownerCommits;
    
    @TableField("other_commits")
    private Integer otherCommits;
    
    @TableField("weekly_commits")
    private Integer weeklyCommits;
    
    @TableField("additions_count")
    private Integer additionsCount;
    
    @TableField("deletions_count")
    private Integer deletionsCount;
    
    // 作者信息
    @TableField("author_login")
    private String authorLogin;
    
    @TableField("author_name")
    private String authorName;
    
    @TableField("author_email")
    private String authorEmail;
    
    // 其他元数据
    @TableField("metadata")
    private String metadata;
    
    // 时间字段
    @TableField("activity_time")
    private LocalDateTime activityTime;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}