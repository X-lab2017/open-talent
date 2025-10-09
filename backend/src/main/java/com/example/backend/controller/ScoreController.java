package com.example.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.common.Result;
import com.example.backend.entity.ContributionScore;
import com.example.backend.service.ScoreCalculationService;
import com.example.backend.service.EvaluationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评分计算和结果展示控制器
 */
@Api(tags = "评分计算和结果展示")
@RestController
@RequestMapping("/scores")
@CrossOrigin
public class ScoreController {
    
    @Autowired
    private ScoreCalculationService scoreCalculationService;
    
    @Autowired
    private EvaluationService evaluationService;
    
    /**
     * 计算课程所有成员的贡献度评分
     */
    @ApiOperation("计算课程所有成员的贡献度评分")
    @PostMapping("/course/{courseId}/calculate")
    public Result<Boolean> calculateCourseScores(@ApiParam("课程ID") @PathVariable Integer courseId) {
        try {
            boolean success = scoreCalculationService.calculateCourseScores(courseId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("课程评分计算失败");
            }
        } catch (Exception e) {
            return Result.error("课程评分计算失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算单个成员的贡献度评分
     */
    @ApiOperation("计算单个成员的贡献度评分")
    @PostMapping("/course/{courseId}/member/{memberId}/calculate")
    public Result<ContributionScore> calculateMemberScore(
            @ApiParam("课程ID") @PathVariable Integer courseId,
            @ApiParam("成员ID") @PathVariable Integer memberId) {
        try {
            Map<String, Object> dimensionWeights = evaluationService.getDimensionWeights(courseId);
            ContributionScore score = scoreCalculationService.calculateMemberScore(courseId, memberId, dimensionWeights);
            if (score != null) {
                return Result.success(score);
            } else {
                return Result.error("成员评分计算失败");
            }
        } catch (Exception e) {
            return Result.error("成员评分计算失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取课程贡献度排行榜
     */
    @ApiOperation("获取课程贡献度排行榜")
    @GetMapping("/course/{courseId}/leaderboard")
    public Result<IPage<ContributionScore>> getCourseLeaderboard(
            @ApiParam("课程ID") @PathVariable Integer courseId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            IPage<ContributionScore> leaderboard = scoreCalculationService.getCourseScoreRanking(courseId, pageNum, pageSize);
            return Result.success(leaderboard);
        } catch (Exception e) {
            return Result.error("查询课程排行榜失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取课程贡献度排行榜（按总分排序）
     */
    @ApiOperation("获取课程贡献度排行榜（按总分排序）")
    @GetMapping("/course/{courseId}/leaderboard/by-score")
    public Result<List<ContributionScore>> getCourseLeaderboardByScore(
            @ApiParam("课程ID") @PathVariable Integer courseId,
            @ApiParam("限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<ContributionScore> leaderboard = scoreCalculationService.getCourseLeaderboardByScore(courseId, limit);
            return Result.success(leaderboard);
        } catch (Exception e) {
            return Result.error("查询课程排行榜失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取成员的贡献度评分详情
     */
    @ApiOperation("获取成员的贡献度评分详情")
    @GetMapping("/course/{courseId}/member/{memberId}")
    public Result<ContributionScore> getMemberScore(
            @ApiParam("课程ID") @PathVariable Integer courseId,
            @ApiParam("成员ID") @PathVariable Integer memberId) {
        try {
            ContributionScore score = scoreCalculationService.getMemberScore(courseId, memberId);
            if (score != null) {
                return Result.success(score);
            } else {
                return Result.error("成员评分不存在");
            }
        } catch (Exception e) {
            return Result.error("查询成员评分失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取成员的各维度得分详情
     */
    @ApiOperation("获取成员的各维度得分详情")
    @GetMapping("/course/{courseId}/member/{memberId}/dimensions")
    public Result<Map<String, Object>> getMemberDimensionScores(
            @ApiParam("课程ID") @PathVariable Integer courseId,
            @ApiParam("成员ID") @PathVariable Integer memberId) {
        try {
            Map<String, Object> dimensionScores = scoreCalculationService.getMemberDimensionScores(courseId, memberId);
            if (dimensionScores != null && !dimensionScores.isEmpty()) {
                return Result.success(dimensionScores);
            } else {
                return Result.error("成员维度得分不存在");
            }
        } catch (Exception e) {
            return Result.error("查询成员维度得分失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取课程所有成员的评分列表
     */
    @ApiOperation("获取课程所有成员的评分列表")
    @GetMapping("/course/{courseId}/all")
    public Result<IPage<ContributionScore>> getAllCourseScores(
            @ApiParam("课程ID") @PathVariable Integer courseId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            IPage<ContributionScore> scores = scoreCalculationService.getAllCourseScores(courseId, pageNum, pageSize);
            return Result.success(scores);
        } catch (Exception e) {
            return Result.error("查询课程评分列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除课程的所有评分记录
     */
    @ApiOperation("删除课程的所有评分记录")
    @DeleteMapping("/course/{courseId}")
    public Result<Boolean> deleteCourseScores(@ApiParam("课程ID") @PathVariable Integer courseId) {
        try {
            boolean success = scoreCalculationService.deleteCourseScores(courseId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("课程评分记录删除失败");
            }
        } catch (Exception e) {
            return Result.error("课程评分记录删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除指定成员的评分记录
     */
    @ApiOperation("删除指定成员的评分记录")
    @DeleteMapping("/course/{courseId}/member/{memberId}")
    public Result<Boolean> deleteMemberScore(
            @ApiParam("课程ID") @PathVariable Integer courseId,
            @ApiParam("成员ID") @PathVariable Integer memberId) {
        try {
            boolean success = scoreCalculationService.deleteMemberScore(courseId, memberId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("成员评分记录删除失败");
            }
        } catch (Exception e) {
            return Result.error("成员评分记录删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取课程评分统计信息
     */
    @ApiOperation("获取课程评分统计信息")
    @GetMapping("/course/{courseId}/statistics")
    public Result<Map<String, Object>> getCourseScoreStatistics(@ApiParam("课程ID") @PathVariable Integer courseId) {
        try {
            Map<String, Object> statistics = scoreCalculationService.getCourseScoreStatistics(courseId);
            if (statistics != null && !statistics.isEmpty()) {
                return Result.success(statistics);
            } else {
                return Result.error("课程评分统计信息不存在");
            }
        } catch (Exception e) {
            return Result.error("查询课程评分统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 重新计算课程排名
     */
    @ApiOperation("重新计算课程排名")
    @PostMapping("/course/{courseId}/recalculate-ranking")
    public Result<Boolean> recalculateCourseRanking(@ApiParam("课程ID") @PathVariable Integer courseId) {
        try {
            boolean success = scoreCalculationService.recalculateCourseRanking(courseId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("课程排名重新计算失败");
            }
        } catch (Exception e) {
            return Result.error("课程排名重新计算失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出课程评分数据
     */
    @ApiOperation("导出课程评分数据")
    @GetMapping("/course/{courseId}/export")
    public Result<List<ContributionScore>> exportCourseScores(@ApiParam("课程ID") @PathVariable Integer courseId) {
        try {
            List<ContributionScore> scores = scoreCalculationService.exportCourseScores(courseId);
            return Result.success(scores);
        } catch (Exception e) {
            return Result.error("课程评分数据导出失败: " + e.getMessage());
        }
    }
}