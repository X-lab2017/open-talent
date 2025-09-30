package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * GitHub配置实体类
 */
@Data
@TableName("github_config")
public class GitHubConfig {
    
    /**
     * 配置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程ID
     */
    @TableField("course_id")
    private Long courseId;
    
    /**
     * 课程名称（非数据库字段，通过JOIN查询获取）
     */
    @TableField(exist = false)
    private String courseName;
    
    /**
     * GitHub访问令牌
     */
    @TableField("github_token")
    private String githubToken;
    
    /**
     * Token状态（active: 有效, inactive: 无效, invalid: 已失效）
     */
    @TableField("token_status")
    private String tokenStatus;
    
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