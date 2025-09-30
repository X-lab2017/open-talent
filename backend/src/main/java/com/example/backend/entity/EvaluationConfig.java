package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价配置实体类
 */
@Data
@TableName("evaluation_configs")
public class EvaluationConfig {
    
    /**
     * 配置ID
     */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Integer configId;
    
    /**
     * 课程ID
     */
    @TableField("course_id")
    private Integer courseId;
    
    /**
     * 配置名称
     */
    @TableField("config_name")
    private String configName;
    
    /**
     * 维度权重配置（JSON格式）
     * 例如：{"code_commits": 0.4, "issue_participation": 0.2, "pr_reviews": 0.2, "documentation": 0.2}
     */
    @TableField("dimension_weights")
    private String dimensionWeights;
    
    
    /**
     * 计算方法（weighted_sum: 加权求和, normalized_score: 标准化评分）
     */
    @TableField("calculation_method")
    private String calculationMethod;
    
    /**
     * 是否激活
     */
    @TableField("is_active")
    private Boolean isActive;
    
    /**
     * 创建者ID
     */
    @TableField("created_by")
    private Integer createdBy;
    
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