package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.entity.GitHubConfig;
import com.example.backend.service.GitHubConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * GitHub配置控制器
 */
@Api(tags = "GitHub配置管理")
@RestController
@RequestMapping("/github-config")
@CrossOrigin
public class GitHubConfigController {
    
    @Autowired
    private GitHubConfigService gitHubConfigService;
    
    /**
     * 保存或更新GitHub配置
     */
    @ApiOperation("保存或更新GitHub配置")
    @PostMapping("/save")
    public Result<Boolean> saveConfig(@RequestBody Map<String, Object> request) {
        try {
            Long courseId = ((Number) request.get("courseId")).longValue();
            String githubToken = (String) request.get("githubToken");
            
            if (courseId == null) {
                return Result.error("课程ID不能为空");
            }
            
            if (githubToken == null || githubToken.trim().isEmpty()) {
                return Result.error("GitHub Token不能为空");
            }
            
            // 验证Token有效性
            boolean isValidToken = gitHubConfigService.validateToken(githubToken);
            if (!isValidToken) {
                return Result.error("GitHub Token无效，请检查后重试");
            }
            
            boolean success = gitHubConfigService.saveOrUpdateConfig(courseId, githubToken);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("GitHub配置保存失败");
            }
        } catch (Exception e) {
            return Result.error("GitHub配置保存失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据课程ID获取GitHub配置
     */
    @ApiOperation("根据课程ID获取GitHub配置")
    @GetMapping("/course/{courseId}")
    public Result<GitHubConfig> getConfigByCourseId(@ApiParam("课程ID") @PathVariable Long courseId) {
        try {
            GitHubConfig config = gitHubConfigService.getConfigByCourseId(courseId);
            if (config != null) {
                // 不返回完整的token，只返回状态信息
                config.setGithubToken(config.getGithubToken() != null ? "已配置" : null);
            }
            return Result.success(config);
        } catch (Exception e) {
            return Result.error("查询GitHub配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有GitHub配置
     */
    @ApiOperation("获取所有GitHub配置")
    @GetMapping("/all")
    public Result<List<GitHubConfig>> getAllConfigs() {
        try {
            List<GitHubConfig> configs = gitHubConfigService.getAllConfigs();
            // 不返回完整的token，只返回状态信息
            for (GitHubConfig config : configs) {
                config.setGithubToken(config.getGithubToken() != null ? "已配置" : null);
            }
            return Result.success(configs);
        } catch (Exception e) {
            return Result.error("查询GitHub配置列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证GitHub Token
     */
    @ApiOperation("验证GitHub Token")
    @PostMapping("/validate")
    public Result<Boolean> validateToken(@RequestBody Map<String, String> request) {
        try {
            String githubToken = request.get("githubToken");
            
            if (githubToken == null || githubToken.trim().isEmpty()) {
                return Result.error("GitHub Token不能为空");
            }
            
            boolean isValid = gitHubConfigService.validateToken(githubToken);
            return Result.success(isValid);
        } catch (Exception e) {
            return Result.error("Token验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证并更新Token状态
     */
    @ApiOperation("验证并更新Token状态")
    @PostMapping("/validate-and-update/{courseId}")
    public Result<Boolean> validateAndUpdateTokenStatus(@ApiParam("课程ID") @PathVariable Long courseId) {
        try {
            boolean isValid = gitHubConfigService.validateAndUpdateTokenStatus(courseId);
            return Result.success(isValid);
        } catch (Exception e) {
            return Result.error("Token状态更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除GitHub配置
     */
    @ApiOperation("删除GitHub配置")
    @DeleteMapping("/course/{courseId}")
    public Result<Boolean> deleteConfig(@ApiParam("课程ID") @PathVariable Long courseId) {
        try {
            boolean success = gitHubConfigService.deleteConfig(courseId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("GitHub配置删除失败");
            }
        } catch (Exception e) {
            return Result.error("GitHub配置删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查课程是否已配置有效的GitHub Token
     */
    @ApiOperation("检查课程是否已配置有效的GitHub Token")
    @GetMapping("/check/{courseId}")
    public Result<Boolean> hasValidToken(@ApiParam("课程ID") @PathVariable Long courseId) {
        try {
            boolean hasValid = gitHubConfigService.hasValidToken(courseId);
            return Result.success(hasValid);
        } catch (Exception e) {
            return Result.error("检查Token状态失败: " + e.getMessage());
        }
    }
}