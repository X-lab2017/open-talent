package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 贡献度评分实体类
 */
@Data
@TableName("contribution_scores")
public class ContributionScore {
    
    /**
     * 评分ID
     */
    @TableId(value = "score_id", type = IdType.AUTO)
    private Long scoreId;
    
    /**
     * 课程ID
     */
    @TableField("course_id")
    private Integer courseId;
    
    /**
     * 成员ID
     */
    @TableField("member_id")
    private Integer memberId;
    
    /**
     * 配置ID
     */
    @TableField("config_id")
    private Integer configId;
    
    /**
     * 总分
     */
    @TableField("total_score")
    private BigDecimal totalScore;
    
    /**
     * 各维度得分（JSON格式）
     * 例如：{"code_commits": 85.5, "issue_participation": 78.2, "pr_reviews": 92.1, "documentation": 65.8}
     */
    @TableField("dimension_scores")
    private String dimensionScores;
    
    /**
     * 排名
     */
    @TableField("ranking")
    private Integer ranking;
    
    /**
     * 计算时间
     */
    @TableField("calculation_time")
    private LocalDateTime calculationTime;
    
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