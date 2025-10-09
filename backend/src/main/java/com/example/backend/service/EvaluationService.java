package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backend.entity.EvaluationConfig;
import com.example.backend.mapper.EvaluationConfigMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 评价配置服务类
 */
@Service
public class EvaluationService {
    
    @Autowired
    private EvaluationConfigMapper evaluationConfigMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 创建评价配置
     * @param courseId 课程ID
     * @param courseName 课程名称
     * @param dimensionWeights 维度权重配置
     * @param calculationMethod 计算方法
     * @return 创建结果
     */
    @Transactional
    public boolean createEvaluationConfig(Integer courseId, String courseName, Map<String, Object> dimensionWeights, String calculationMethod, Integer organizationId) {
        try {
            System.out.println("开始创建评价配置: courseId=" + courseId + ", courseName=" + courseName + ", calculationMethod=" + calculationMethod);
            
            EvaluationConfig config = new EvaluationConfig();
            config.setCourseId(courseId);
            // 使用课程名称设置配置名称，如果课程名称为空则使用默认格式
            String configName = (courseName != null && !courseName.trim().isEmpty()) 
                ? courseName + "评价配置" 
                : "课程" + courseId + "评价配置";
            config.setConfigName(configName);
            config.setDimensionWeights(objectMapper.writeValueAsString(dimensionWeights));
            config.setCalculationMethod(calculationMethod != null ? calculationMethod : "weighted_average"); // 使用传入的计算方法或默认值
            config.setIsActive(true); // 设置为激活状态
            config.setCreatedBy(organizationId);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            
            System.out.println("准备插入配置: " + config);
            int result = evaluationConfigMapper.insert(config);
            System.out.println("插入结果: " + result);
            
            return result > 0;
        } catch (JsonProcessingException e) {
            System.err.println("JSON处理异常: " + e.getMessage());
            throw new RuntimeException("评价配置创建失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("创建配置异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("评价配置创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建评价配置（兼容旧版本）
     * @param courseId 课程ID
     * @param dimensionWeights 维度权重配置
     * @param calculationMethod 计算方法
     * @return 创建结果
     */
    @Transactional
    public boolean createEvaluationConfig(Integer courseId, Map<String, Object> dimensionWeights, String calculationMethod, Integer organizationId) {
        return createEvaluationConfig(courseId, null, dimensionWeights, calculationMethod, organizationId);
    }
    
    /**
     * 创建评价配置（兼容旧版本）
     * @param courseId 课程ID
     * @param dimensionWeights 维度权重配置
     * @return 创建结果
     */
    @Transactional
    public boolean createEvaluationConfig(Integer courseId, Map<String, Object> dimensionWeights, Integer organizationId) {
        return createEvaluationConfig(courseId, null, dimensionWeights, "weighted_average", organizationId);
    }
    
    /**
     * 更新评价配置
     * @param configId 配置ID
     * @param dimensionWeights 维度权重配置
     * @return 更新结果
     */
    @Transactional
    public boolean updateEvaluationConfig(Integer configId, Map<String, Object> dimensionWeights) {
        try {
            EvaluationConfig config = evaluationConfigMapper.selectById(configId);
            if (config != null) {
                config.setDimensionWeights(objectMapper.writeValueAsString(dimensionWeights));
                config.setUpdatedAt(LocalDateTime.now());
                return evaluationConfigMapper.updateById(config) > 0;
            }
            return false;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("评价配置更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据课程ID获取评价配置
     * @param courseId 课程ID
     * @return 评价配置
     */
    public EvaluationConfig getEvaluationConfigByCourseId(Integer courseId) {
        return evaluationConfigMapper.selectByCourseId(courseId);
    }
    
    /**
     * 获取评价配置的维度权重
     * @param courseId 课程ID
     * @return 维度权重Map
     */
    public Map<String, Object> getDimensionWeights(Integer courseId) {
        EvaluationConfig config = getEvaluationConfigByCourseId(courseId);
        if (config != null && config.getDimensionWeights() != null) {
            try {
                return objectMapper.readValue(config.getDimensionWeights(), Map.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("解析维度权重配置失败: " + e.getMessage());
            }
        }
        return getDefaultDimensionWeights();
    }
    
    /**
     * 获取默认维度权重配置
     * @return 默认权重配置
     */
    public Map<String, Object> getDefaultDimensionWeights() {
        Map<String, Object> defaultWeights = new HashMap<>();
        defaultWeights.put("commit_frequency", 0.3);
        defaultWeights.put("code_quality", 0.25);
        defaultWeights.put("collaboration", 0.2);
        defaultWeights.put("innovation", 0.15);
        defaultWeights.put("documentation", 0.1);
        return defaultWeights;
    }
    
    /**
     * 删除评价配置
     * @param configId 配置ID
     * @return 删除结果
     */
    @Transactional
    public boolean deleteEvaluationConfig(Integer configId) {
        return evaluationConfigMapper.deleteById(configId) > 0;
    }
    
    /**
     * 根据课程ID删除评价配置
     * @param courseId 课程ID
     * @return 删除结果
     */
    @Transactional
    public boolean deleteEvaluationConfigByCourseId(Integer courseId) {
        QueryWrapper<EvaluationConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("course_id", courseId);
        return evaluationConfigMapper.delete(wrapper) > 0;
    }
    
    /**
     * 验证维度权重配置
     * @param dimensionWeights 维度权重配置
     * @return 验证结果
     */
    public boolean validateDimensionWeights(Map<String, Object> dimensionWeights) {
        if (dimensionWeights == null || dimensionWeights.isEmpty()) {
            System.out.println("权重验证失败: 权重配置为空");
            return false;
        }
        
        double totalWeight = 0.0;
        System.out.println("开始验证权重配置: " + dimensionWeights);
        
        for (Map.Entry<String, Object> entry : dimensionWeights.entrySet()) {
            Object weight = entry.getValue();
            if (weight instanceof Number) {
                double w = ((Number) weight).doubleValue();
                System.out.println("权重项 " + entry.getKey() + ": " + w + " (类型: " + weight.getClass().getSimpleName() + ")");
                if (w < 0 || w > 1) {
                    System.out.println("权重验证失败: 权重值超出范围 [0,1]: " + w);
                    return false;
                }
                totalWeight += w;
            } else {
                System.out.println("权重验证失败: 权重值不是数字类型: " + weight + " (类型: " + weight.getClass().getSimpleName() + ")");
                return false;
            }
        }
        
        double difference = Math.abs(totalWeight - 1.0);
        System.out.println("权重总和: " + totalWeight + ", 与1.0的差值: " + difference);
        
        // 权重总和应该等于1.0（允许小的浮点误差）
        // 调整误差范围以匹配前端精度处理
        boolean isValid = difference < 0.0001;
        System.out.println("权重验证结果: " + (isValid ? "通过" : "失败"));
        
        return isValid;
    }
}