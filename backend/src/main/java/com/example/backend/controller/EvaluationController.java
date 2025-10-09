package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.entity.EvaluationConfig;
import com.example.backend.service.EvaluationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评价配置控制器
 */
@Api(tags = "评价配置管理")
@RestController
@RequestMapping("/evaluation")
@CrossOrigin
public class EvaluationController {
    
    @Autowired
    private EvaluationService evaluationService;
    
    /**
     * 创建评价配置
     */
    @ApiOperation("创建评价配置")
    @PostMapping("/config")
    public Result<Boolean> createEvaluationConfig(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("收到创建配置请求: " + request);
            
            // 安全地处理类型转换
            Integer courseId = null;
            Object courseIdObj = request.get("courseId");
            if (courseIdObj instanceof String) {
                courseId = Integer.parseInt((String) courseIdObj);
            } else if (courseIdObj instanceof Integer) {
                courseId = (Integer) courseIdObj;
            }
            
            String courseName = (String) request.get("courseName");
            
            Integer organizationId = null;
            Object organizationIdObj = request.get("organizationId");
            if (organizationIdObj instanceof String) {
                organizationId = Integer.parseInt((String) organizationIdObj);
            } else if (organizationIdObj instanceof Integer) {
                organizationId = (Integer) organizationIdObj;
            }
            String calculationMethod = (String) request.get("calculationMethod");
            @SuppressWarnings("unchecked")
            Map<String, Object> dimensionWeights = (Map<String, Object>) request.get("dimensionWeights");
            
            System.out.println("解析的参数: courseId=" + courseId + ", courseName=" + courseName + ", organizationId=" + organizationId + ", calculationMethod=" + calculationMethod + ", dimensionWeights=" + dimensionWeights);
            
            // 验证权重配置
            if (!evaluationService.validateDimensionWeights(dimensionWeights)) {
                return Result.error("维度权重配置无效，权重总和必须等于1.0");
            }
            
            boolean success = evaluationService.createEvaluationConfig(courseId, courseName, dimensionWeights, calculationMethod, organizationId);
            if (success) {
                System.out.println("配置创建成功");
                return Result.success(true);
            } else {
                System.out.println("配置创建失败");
                return Result.error("评价配置创建失败");
            }
        } catch (Exception e) {
            System.err.println("创建配置异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("评价配置创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新评价配置
     */
    @ApiOperation("更新评价配置")
    @PutMapping("/config/{configId}")
    public Result<Boolean> updateEvaluationConfig(
            @ApiParam("配置ID") @PathVariable Integer configId,
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dimensionWeights = (Map<String, Object>) request.get("dimensionWeights");
            
            // 验证权重配置
            if (!evaluationService.validateDimensionWeights(dimensionWeights)) {
                return Result.error("维度权重配置无效，权重总和必须等于1.0");
            }
            
            boolean success = evaluationService.updateEvaluationConfig(configId, dimensionWeights);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("评价配置更新失败");
            }
        } catch (Exception e) {
            return Result.error("评价配置更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据课程ID获取评价配置
     */
    @ApiOperation("根据课程ID获取评价配置")
    @GetMapping("/config/course/{courseId}")
    public Result<EvaluationConfig> getEvaluationConfigByCourseId(@ApiParam("课程ID") @PathVariable Integer courseId) {
        try {
            EvaluationConfig config = evaluationService.getEvaluationConfigByCourseId(courseId);
            if (config != null) {
                return Result.success(config);
            } else {
                return Result.error("评价配置不存在");
            }
        } catch (Exception e) {
            return Result.error("查询评价配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取评价配置的维度权重
     */
    @ApiOperation("获取评价配置的维度权重")
    @GetMapping("/config/course/{courseId}/weights")
    public Result<Map<String, Object>> getDimensionWeights(@ApiParam("课程ID") @PathVariable Integer courseId) {
        try {
            Map<String, Object> weights = evaluationService.getDimensionWeights(courseId);
            return Result.success(weights);
        } catch (Exception e) {
            return Result.error("查询维度权重失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取默认维度权重配置
     */
    @ApiOperation("获取默认维度权重配置")
    @GetMapping("/config/default-weights")
    public Result<Map<String, Object>> getDefaultDimensionWeights() {
        try {
            Map<String, Object> defaultWeights = evaluationService.getDefaultDimensionWeights();
            return Result.success(defaultWeights);
        } catch (Exception e) {
            return Result.error("查询默认权重配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除评价配置
     */
    @ApiOperation("删除评价配置")
    @DeleteMapping("/config/{configId}")
    public Result<Boolean> deleteEvaluationConfig(@ApiParam("配置ID") @PathVariable Integer configId) {
        try {
            boolean success = evaluationService.deleteEvaluationConfig(configId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("评价配置删除失败");
            }
        } catch (Exception e) {
            return Result.error("评价配置删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据课程ID删除评价配置
     */
    @ApiOperation("根据课程ID删除评价配置")
    @DeleteMapping("/config/course/{courseId}")
    public Result<Boolean> deleteEvaluationConfigByCourseId(@ApiParam("课程ID") @PathVariable Integer courseId) {
        try {
            boolean success = evaluationService.deleteEvaluationConfigByCourseId(courseId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("评价配置删除失败");
            }
        } catch (Exception e) {
            return Result.error("评价配置删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证维度权重配置
     */
    @ApiOperation("验证维度权重配置")
    @PostMapping("/config/validate-weights")
    public Result<Boolean> validateDimensionWeights(@RequestBody Map<String, Object> dimensionWeights) {
        try {
            boolean isValid = evaluationService.validateDimensionWeights(dimensionWeights);
            if (isValid) {
                return Result.success(true);
            } else {
                return Result.success(false);
            }
        } catch (Exception e) {
            return Result.error("验证维度权重配置失败: " + e.getMessage());
        }
    }
}