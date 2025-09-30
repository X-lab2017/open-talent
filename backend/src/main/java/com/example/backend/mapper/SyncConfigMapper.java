package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.SyncConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 同步配置Mapper接口
 */
@Mapper
public interface SyncConfigMapper extends BaseMapper<SyncConfig> {
    
    /**
     * 根据配置键查询配置
     * @param configKey 配置键
     * @return 配置信息
     */
    @Select("SELECT * FROM sync_config WHERE config_key = #{configKey}")
    SyncConfig selectByConfigKey(@Param("configKey") String configKey);
    
    /**
     * 根据配置键更新配置值
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 更新结果
     */
    @Update("UPDATE sync_config SET config_value = #{configValue}, updated_at = #{updatedAt} WHERE config_key = #{configKey}")
    int updateValueByKey(@Param("configKey") String configKey, 
                        @Param("configValue") String configValue, 
                        @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 获取所有启用的配置
     * @return 启用的配置列表
     */
    @Select("SELECT * FROM sync_config WHERE enabled = 1 ORDER BY config_key")
    List<SyncConfig> selectEnabledConfigs();
    
    /**
     * 根据配置键启用或禁用配置
     * @param configKey 配置键
     * @param enabled 是否启用
     * @return 更新结果
     */
    @Update("UPDATE sync_config SET enabled = #{enabled}, updated_at = #{updatedAt} WHERE config_key = #{configKey}")
    int updateEnabledByKey(@Param("configKey") String configKey, 
                          @Param("enabled") Boolean enabled, 
                          @Param("updatedAt") LocalDateTime updatedAt);
}