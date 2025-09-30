package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.*;
import com.example.backend.mapper.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 评分计算服务类
 */
@Service
public class ScoreCalculationService {
    
    @Autowired
    private ContributionScoreMapper contributionScoreMapper;
    
    @Autowired
    private GitActivityMapper gitActivityMapper;
    
    @Autowired
    private CourseEnrollmentMapper courseEnrollmentMapper;
    
    @Autowired
    private EvaluationService evaluationService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 计算课程所有学生的贡献度评分
     * @param courseId 课程ID
     * @return 计算结果
     */
    @Transactional
    public boolean calculateCourseScores(Integer courseId) {
        try {
            // 获取课程学生列表
            List<CourseEnrollment> students = courseEnrollmentMapper.selectByCourseId(courseId);
            if (students.isEmpty()) {
                return true;
            }
            
            // 获取评价配置
            Map<String, Object> dimensionWeights = evaluationService.getDimensionWeights(courseId);
            
            // 计算每个学生的评分
            List<ContributionScore> scores = new ArrayList<>();
            for (CourseEnrollment student : students) {
                ContributionScore score = calculateMemberScore(courseId, student.getMemberId(), dimensionWeights);
                if (score != null) {
                    scores.add(score);
                }
            }
            
            // 排序并设置排名
            scores.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));
            for (int i = 0; i < scores.size(); i++) {
                scores.get(i).setRanking(i + 1);
            }
            
            // 保存或更新评分记录
            for (ContributionScore score : scores) {
                saveOrUpdateScore(score);
            }
            
            return true;
        } catch (Exception e) {
            throw new RuntimeException("计算课程评分失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算单个成员的贡献度评分
     * @param courseId 课程ID
     * @param memberId 成员ID
     * @param dimensionWeights 维度权重配置
     * @return 贡献度评分
     */
    public ContributionScore calculateMemberScore(Integer courseId, Integer memberId, Map<String, Object> dimensionWeights) {
        try {
            // 获取成员的Git活动数据
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minus(30, ChronoUnit.DAYS); // 最近30天
            
            List<GitActivity> activities = gitActivityMapper.selectByMemberId(memberId);
            
            // 计算各维度得分
            Map<String, Double> dimensionScores = new HashMap<>();
            dimensionScores.put("commit_frequency", calculateCommitFrequencyScore(activities));
            dimensionScores.put("code_quality", calculateCodeQualityScore(activities));
            dimensionScores.put("collaboration", calculateCollaborationScore(activities));
            dimensionScores.put("innovation", calculateInnovationScore(activities));
            dimensionScores.put("documentation", calculateDocumentationScore(activities));
            
            // 计算总分
            BigDecimal totalScore = BigDecimal.ZERO;
            for (Map.Entry<String, Double> entry : dimensionScores.entrySet()) {
                String dimension = entry.getKey();
                Double score = entry.getValue();
                Double weight = (Double) dimensionWeights.getOrDefault(dimension, 0.0);
                totalScore = totalScore.add(BigDecimal.valueOf(score * weight));
            }
            
            // 创建评分记录
            ContributionScore contributionScore = new ContributionScore();
            contributionScore.setCourseId(courseId);
            contributionScore.setMemberId(memberId);
            contributionScore.setTotalScore(totalScore);
            contributionScore.setDimensionScores(objectMapper.writeValueAsString(dimensionScores));
            contributionScore.setCalculationTime(LocalDateTime.now());
            
            return contributionScore;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("计算成员评分失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算提交频率得分
     * @param activities Git活动列表
     * @return 提交频率得分 (0-100)
     */
    private double calculateCommitFrequencyScore(List<GitActivity> activities) {
        long commitCount = activities.stream()
                .filter(activity -> "commit".equals(activity.getActivityType()))
                .count();
        
        // 基于提交次数计算得分，最高100分
        return Math.min(commitCount * 5.0, 100.0);
    }
    
    /**
     * 计算代码质量得分
     * @param activities Git活动列表
     * @return 代码质量得分 (0-100)
     */
    private double calculateCodeQualityScore(List<GitActivity> activities) {
        // 基于代码行数变化、文件修改等计算质量得分
        int totalLinesAdded = activities.stream()
                .filter(activity -> "commit".equals(activity.getActivityType()))
                .mapToInt(activity -> activity.getLinesAdded() != null ? activity.getLinesAdded() : 0)
                .sum();
        
        int totalLinesDeleted = activities.stream()
                .filter(activity -> "commit".equals(activity.getActivityType()))
                .mapToInt(activity -> activity.getLinesDeleted() != null ? activity.getLinesDeleted() : 0)
                .sum();
        
        // 简单的质量评估：添加代码多于删除代码得分更高
        double ratio = totalLinesDeleted > 0 ? (double) totalLinesAdded / totalLinesDeleted : totalLinesAdded;
        return Math.min(ratio * 10.0, 100.0);
    }
    
    /**
     * 计算协作得分
     * @param activities Git活动列表
     * @return 协作得分 (0-100)
     */
    private double calculateCollaborationScore(List<GitActivity> activities) {
        // 基于PR、Issue、Review等协作活动计算得分
        long collaborationCount = activities.stream()
                .filter(activity -> Arrays.asList("pull_request", "issue", "review").contains(activity.getActivityType()))
                .count();
        
        return Math.min(collaborationCount * 10.0, 100.0);
    }
    
    /**
     * 计算创新得分
     * @param activities Git活动列表
     * @return 创新得分 (0-100)
     */
    private double calculateInnovationScore(List<GitActivity> activities) {
        // 基于新功能、新文件创建等计算创新得分
        long newFileCount = activities.stream()
                .filter(activity -> "commit".equals(activity.getActivityType()))
                .count(); // 简化为提交次数作为新文件指标
        
        return Math.min(newFileCount * 3.0, 100.0);
    }
    
    /**
     * 计算文档得分
     * @param activities Git活动列表
     * @return 文档得分 (0-100)
     */
    private double calculateDocumentationScore(List<GitActivity> activities) {
        // 基于README、注释、文档文件的修改计算得分
        long docCommitCount = activities.stream()
                .filter(activity -> "commit".equals(activity.getActivityType()))
                .filter(activity -> activity.getCommitMessage() != null && 
                        (activity.getCommitMessage().toLowerCase().contains("doc") ||
                         activity.getCommitMessage().toLowerCase().contains("readme") ||
                         activity.getCommitMessage().toLowerCase().contains("comment")))
                .count();
        
        return Math.min(docCommitCount * 15.0, 100.0);
    }
    
    /**
     * 保存或更新评分记录
     * @param score 评分记录
     * @return 保存结果
     */
    @Transactional
    public boolean saveOrUpdateScore(ContributionScore score) {
        ContributionScore existing = contributionScoreMapper.selectByCourseIdAndMemberId(score.getCourseId(), score.getMemberId());
        if (existing != null) {
            score.setScoreId(existing.getScoreId());
            return contributionScoreMapper.updateById(score) > 0;
        } else {
            return contributionScoreMapper.insert(score) > 0;
        }
    }
    
    /**
     * 获取课程评分排行榜
     * @param courseId 课程ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 评分排行榜
     */
    public IPage<ContributionScore> getCourseScoreRanking(Integer courseId, Integer pageNum, Integer pageSize) {
        Page<ContributionScore> page = new Page<>(pageNum, pageSize);
        return contributionScoreMapper.selectByCourseIdOrderByRanking(page, courseId);
    }
    
    /**
     * 获取课程所有成员的评分列表
     * @param courseId 课程ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 评分列表
     */
    public IPage<ContributionScore> getAllCourseScores(Integer courseId, Integer pageNum, Integer pageSize) {
        Page<ContributionScore> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ContributionScore> wrapper = new QueryWrapper<>();
        wrapper.eq("course_id", courseId).orderByDesc("total_score");
        return contributionScoreMapper.selectPage(page, wrapper);
    }
    
    /**
     * 获取课程贡献度排行榜（限制数量）
     * @param courseId 课程ID
     * @param limit 限制返回数量
     * @return 贡献度排行榜列表
     */
    public List<ContributionScore> getCourseLeaderboardByScore(Integer courseId, Integer limit) {
        Page<ContributionScore> page = new Page<>(1, limit);
        IPage<ContributionScore> result = contributionScoreMapper.selectByCourseIdOrderByRanking(page, courseId);
        return result.getRecords();
    }
    
    /**
     * 获取成员评分详情
     * @param courseId 课程ID
     * @param memberId 成员ID
     * @return 评分详情
     */
    public ContributionScore getMemberScore(Integer courseId, Integer memberId) {
        return contributionScoreMapper.selectByCourseIdAndMemberId(courseId, memberId);
    }
    
    /**
     * 获取成员各维度得分
     * @param courseId 课程ID
     * @param memberId 成员ID
     * @return 各维度得分Map
     */
    public Map<String, Object> getMemberDimensionScores(Integer courseId, Integer memberId) {
        ContributionScore score = getMemberScore(courseId, memberId);
        if (score != null && score.getDimensionScores() != null) {
            try {
                return objectMapper.readValue(score.getDimensionScores(), Map.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("解析维度得分失败: " + e.getMessage());
            }
        }
        return new HashMap<>();
    }
    
    /**
     * 删除课程评分记录
     * @param courseId 课程ID
     * @return 删除结果
     */
    @Transactional
    public boolean deleteCourseScores(Integer courseId) {
        QueryWrapper<ContributionScore> wrapper = new QueryWrapper<>();
        wrapper.eq("course_id", courseId);
        return contributionScoreMapper.delete(wrapper) > 0;
    }
    
    /**
     * 删除指定成员的评分记录
     * @param courseId 课程ID
     * @param memberId 成员ID
     * @return 删除结果
     */
    @Transactional
    public boolean deleteMemberScore(Integer courseId, Integer memberId) {
        QueryWrapper<ContributionScore> wrapper = new QueryWrapper<>();
        wrapper.eq("course_id", courseId).eq("member_id", memberId);
        return contributionScoreMapper.delete(wrapper) > 0;
    }
    
    /**
     * 获取课程评分统计信息
     * @param courseId 课程ID
     * @return 统计信息
     */
    public Map<String, Object> getCourseScoreStatistics(Integer courseId) {
        QueryWrapper<ContributionScore> wrapper = new QueryWrapper<>();
        wrapper.eq("course_id", courseId);
        List<ContributionScore> scores = contributionScoreMapper.selectList(wrapper);
        
        Map<String, Object> statistics = new HashMap<>();
        if (scores.isEmpty()) {
            statistics.put("totalStudents", 0);
            statistics.put("averageScore", 0.0);
            statistics.put("maxScore", 0.0);
            statistics.put("minScore", 0.0);
            return statistics;
        }
        
        double totalScore = scores.stream().mapToDouble(score -> score.getTotalScore().doubleValue()).sum();
        double averageScore = totalScore / scores.size();
        double maxScore = scores.stream().mapToDouble(score -> score.getTotalScore().doubleValue()).max().orElse(0.0);
        double minScore = scores.stream().mapToDouble(score -> score.getTotalScore().doubleValue()).min().orElse(0.0);
        
        statistics.put("totalStudents", scores.size());
        statistics.put("averageScore", averageScore);
        statistics.put("maxScore", maxScore);
        statistics.put("minScore", minScore);
        statistics.put("totalScore", totalScore);
        
        return statistics;
    }
    
    /**
     * 重新计算课程排名
     * @param courseId 课程ID
     * @return 计算结果
     */
    @Transactional
    public boolean recalculateCourseRanking(Integer courseId) {
        try {
            QueryWrapper<ContributionScore> wrapper = new QueryWrapper<>();
            wrapper.eq("course_id", courseId).orderByDesc("total_score");
            List<ContributionScore> scores = contributionScoreMapper.selectList(wrapper);
            
            // 重新设置排名
            for (int i = 0; i < scores.size(); i++) {
                ContributionScore score = scores.get(i);
                score.setRanking(i + 1);
                contributionScoreMapper.updateById(score);
            }
            
            return true;
        } catch (Exception e) {
            throw new RuntimeException("重新计算课程排名失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出课程评分数据
     * @param courseId 课程ID
     * @return 评分数据列表
     */
    public List<ContributionScore> exportCourseScores(Integer courseId) {
        QueryWrapper<ContributionScore> wrapper = new QueryWrapper<>();
        wrapper.eq("course_id", courseId).orderByAsc("ranking");
        return contributionScoreMapper.selectList(wrapper);
    }
}