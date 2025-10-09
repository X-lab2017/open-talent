-- 创建同步配置表
CREATE TABLE IF NOT EXISTS sync_config (
    config_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键名',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) COMMENT '配置描述',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同步配置表';

-- 插入默认配置数据
INSERT INTO sync_config (config_key, config_value, description, enabled) VALUES
('sync_frequency', 'daily', '同步频率设置', TRUE),
('auto_sync', 'true', '是否启用自动同步', TRUE),
('max_retries', '3', '最大重试次数', TRUE),
('timeout', '30000', '请求超时时间(毫秒)', TRUE),
('batch_size', '100', '批处理大小', TRUE)
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    description = VALUES(description),
    updated_at = CURRENT_TIMESTAMP;