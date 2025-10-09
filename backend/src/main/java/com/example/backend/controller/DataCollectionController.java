package com.example.backend.controller;

import com.example.backend.entity.SyncConfig;
import com.example.backend.entity.GitRepository;
import com.example.backend.mapper.SyncConfigMapper;
import com.example.backend.service.DataCollectionService;
import com.example.backend.service.GitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据收集控制器
 */
@RestController
@RequestMapping("/data-collection")
@CrossOrigin(origins = "*")
public class DataCollectionController {
    
    private static final Logger logger = LoggerFactory.getLogger(DataCollectionController.class);
    
    @Autowired
    private DataCollectionService dataCollectionService;
    
    @Autowired
    private GitDataService gitDataService;
    
    @Autowired
    private SyncConfigMapper syncConfigMapper;
    
    /**
     * 获取数据收集状态
     * @return 数据收集状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDataCollectionStatus() {
        try {
            logger.info("获取数据收集状态");
            
            Map<String, Object> response = dataCollectionService.getDataCollectionStatus();
            
            logger.info("数据收集状态获取成功");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取数据收集状态失败: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取数据收集状态失败: " + e.getMessage());
            errorResponse.put("stats", new HashMap<String, Object>() {{
                put("totalRepos", 0);
                put("syncedRepos", 0);
                put("totalCommits", 0);
                put("processedData", 0);
            }});
            errorResponse.put("tasks", new java.util.ArrayList<>());
            errorResponse.put("logs", new java.util.ArrayList<>());
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * 获取同步配置
     * @return 同步配置信息
     */
    @GetMapping("/sync-config")
    public ResponseEntity<Map<String, Object>> getSyncConfig() {
        try {
            logger.info("获取同步配置");
            
            Map<String, Object> config = new HashMap<>();
            
            // 获取所有配置项
            List<SyncConfig> configs = syncConfigMapper.selectEnabledConfigs();
            
            // 设置默认配置
            config.put("syncFrequency", "daily");
            config.put("autoSync", true);
            config.put("maxRetries", 3);
            config.put("timeout", 30000);
            config.put("batchSize", 100);
            
            // 从数据库配置覆盖默认值
            for (SyncConfig syncConfig : configs) {
                String key = syncConfig.getConfigKey();
                String value = syncConfig.getConfigValue();
                
                switch (key) {
                    case "sync_frequency":
                        config.put("syncFrequency", value);
                        break;
                    case "auto_sync":
                        config.put("autoSync", Boolean.parseBoolean(value));
                        break;
                    case "max_retries":
                        config.put("maxRetries", Integer.parseInt(value));
                        break;
                    case "timeout":
                        config.put("timeout", Integer.parseInt(value));
                        break;
                    case "batch_size":
                        config.put("batchSize", Integer.parseInt(value));
                        break;
                }
            }
            
            logger.info("同步配置获取成功: {}", config);
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            logger.error("获取同步配置失败: {}", e.getMessage(), e);
            
            // 返回默认配置
            Map<String, Object> defaultConfig = new HashMap<>();
            defaultConfig.put("syncFrequency", "daily");
            defaultConfig.put("autoSync", true);
            defaultConfig.put("maxRetries", 3);
            defaultConfig.put("timeout", 30000);
            defaultConfig.put("batchSize", 100);
            defaultConfig.put("error", "获取配置失败，返回默认配置: " + e.getMessage());
            
            return ResponseEntity.ok(defaultConfig);
        }
    }
    
    /**
     * 保存同步配置
     * @param configData 配置数据
     * @return 保存结果
     */
    @PostMapping("/sync-config")
    public ResponseEntity<Map<String, Object>> saveSyncConfig(@RequestBody Map<String, Object> configData) {
        try {
            logger.info("保存同步配置: {}", configData);
            
            Map<String, Object> response = new HashMap<>();
            
            // 保存各个配置项
            for (Map.Entry<String, Object> entry : configData.entrySet()) {
                String key = entry.getKey();
                String value = String.valueOf(entry.getValue());
                
                String configKey = convertToConfigKey(key);
                if (configKey != null) {
                    saveOrUpdateConfig(configKey, value, getConfigDescription(key));
                }
            }
            
            response.put("success", true);
            response.put("message", "同步配置保存成功");
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("同步配置保存成功");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("保存同步配置失败: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "保存同步配置失败: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * 转换前端配置键名为数据库配置键名
     * @param frontendKey 前端键名
     * @return 数据库键名
     */
    private String convertToConfigKey(String frontendKey) {
        switch (frontendKey) {
            case "syncFrequency":
                return "sync_frequency";
            case "autoSync":
                return "auto_sync";
            case "maxRetries":
                return "max_retries";
            case "timeout":
                return "timeout";
            case "batchSize":
                return "batch_size";
            default:
                return null;
        }
    }
    
    /**
     * 获取配置项描述
     * @param key 配置键
     * @return 描述
     */
    private String getConfigDescription(String key) {
        switch (key) {
            case "syncFrequency":
                return "同步频率设置";
            case "autoSync":
                return "是否启用自动同步";
            case "maxRetries":
                return "最大重试次数";
            case "timeout":
                return "请求超时时间(毫秒)";
            case "batchSize":
                return "批处理大小";
            default:
                return "配置项";
        }
    }
    
    /**
     * 获取仓库列表（用于数据采集界面选择）
     * @param courseId 课程ID（可选）
     * @return 仓库列表
     */
    @GetMapping("/repositories")
    public ResponseEntity<Map<String, Object>> getRepositoriesForCollection(
            @RequestParam(required = false) String courseId) {
        try {
            logger.info("获取数据采集仓库列表，课程ID: {}", courseId);
            
            List<GitRepository> repositories;
            if (courseId != null && !courseId.trim().isEmpty()) {
                try {
                    Integer courseIdInt = Integer.parseInt(courseId.trim());
                    repositories = gitDataService.getRepositoriesByCourseId(courseIdInt);
                } catch (NumberFormatException e) {
                    logger.error("无效的课程ID格式: {}", courseId, e);
                    throw new IllegalArgumentException("课程ID必须是有效的数字: " + courseId);
                }
            } else {
                repositories = gitDataService.getAllRepositories();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", repositories);
            response.put("total", repositories.size());
            
            logger.info("获取数据采集仓库列表成功，共{}个仓库", repositories.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取数据采集仓库列表失败: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取仓库列表失败: " + e.getMessage());
            errorResponse.put("data", new java.util.ArrayList<>());
            errorResponse.put("total", 0);
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * 保存或更新配置
     * @param configKey 配置键
     * @param configValue 配置值
     * @param description 描述
     */
    private void saveOrUpdateConfig(String configKey, String configValue, String description) {
        try {
            SyncConfig existingConfig = syncConfigMapper.selectByConfigKey(configKey);
            
            if (existingConfig != null) {
                // 更新现有配置
                syncConfigMapper.updateValueByKey(configKey, configValue, LocalDateTime.now());
            } else {
                // 创建新配置
                SyncConfig newConfig = new SyncConfig(configKey, configValue, description, true);
                syncConfigMapper.insert(newConfig);
            }
        } catch (Exception e) {
            logger.error("保存配置失败: {} = {}, 错误: {}", configKey, configValue, e.getMessage());
        }
    }
}