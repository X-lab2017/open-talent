-- 学生贡献度计算模块数据库表结构
-- 创建时间: 2024-01-20
-- 描述: 包含课程管理、Git数据采集、评价配置、贡献度评分等功能的数据库表

-- 1. 课程表
CREATE TABLE IF NOT EXISTS courses (
    course_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '课程ID',
    organization_id INT NOT NULL COMMENT '组织ID',
    course_name VARCHAR(255) NOT NULL COMMENT '课程名称',
    course_description TEXT COMMENT '课程描述',
    teacher_id INT NOT NULL COMMENT '教师ID',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    status ENUM('active', 'inactive', 'archived') DEFAULT 'active' COMMENT '课程状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_organization_id (organization_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 2. 课程注册表
CREATE TABLE IF NOT EXISTS course_enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '注册ID',
    course_id INT NOT NULL COMMENT '课程ID',
    member_id INT NOT NULL COMMENT '成员ID',
    member_name VARCHAR(255) NOT NULL COMMENT '成员姓名',
    member_email VARCHAR(255) COMMENT '成员邮箱',
    student_id VARCHAR(50) COMMENT '学号',
    group_name VARCHAR(255) COMMENT '分组名称',
    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    status ENUM('active', 'inactive', 'dropped') DEFAULT 'active' COMMENT '注册状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_course_member (course_id, member_id),
    INDEX idx_course_id (course_id),
    INDEX idx_member_id (member_id),
    INDEX idx_group_name (group_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程注册表';

-- 3. 评价配置表
CREATE TABLE IF NOT EXISTS evaluation_configs (
    config_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    course_id INT NOT NULL COMMENT '课程ID',
    config_name VARCHAR(255) NOT NULL COMMENT '配置名称',
    dimension_weights JSON NOT NULL COMMENT '维度权重配置(JSON格式)',
    calculation_method ENUM('weighted_average', 'linear_combination', 'custom') DEFAULT 'weighted_average' COMMENT '计算方法',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_by INT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_course_id (course_id),
    INDEX idx_is_active (is_active),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价配置表';

-- 4. Git仓库表
CREATE TABLE IF NOT EXISTS git_repositories (
    repository_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '仓库ID',
    course_id INT NOT NULL COMMENT '课程ID',
    repository_name VARCHAR(255) NOT NULL COMMENT '仓库名称',
    repository_url VARCHAR(500) NOT NULL COMMENT '仓库URL',
    platform VARCHAR(50) NOT NULL COMMENT '平台类型(github, gitlab, gitee等)',
    access_token VARCHAR(500) COMMENT '访问令牌',
    branch_name VARCHAR(255) DEFAULT 'main' COMMENT '分支名称',
    last_sync_time TIMESTAMP NULL COMMENT '最后同步时间',
    sync_status ENUM('pending', 'syncing', 'success', 'failed') DEFAULT 'pending' COMMENT '同步状态',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_repository_url (repository_url),
    INDEX idx_course_id (course_id),
    INDEX idx_platform (platform),
    INDEX idx_sync_status (sync_status),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Git仓库表';

-- 5. GitHub配置表
CREATE TABLE IF NOT EXISTS github_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    course_id INT NOT NULL COMMENT '课程ID',
    github_token VARCHAR(255) COMMENT 'GitHub访问令牌',
    token_status VARCHAR(50) DEFAULT 'inactive' COMMENT 'Token状态(active, inactive, invalid)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_course_github (course_id),
    INDEX idx_course_id (course_id),
    INDEX idx_token_status (token_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GitHub配置表';

-- 6. Git活动记录表
CREATE TABLE IF NOT EXISTS git_activities (
    activity_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '活动ID',
    repository_id BIGINT NOT NULL COMMENT '仓库ID',
    member_id BIGINT COMMENT '成员ID',
    activity_type VARCHAR(50) NOT NULL COMMENT '活动类型',
    
    -- 提交相关字段
    commit_hash VARCHAR(40) COMMENT '提交哈希',
    lines_added INT COMMENT '新增行数',
    lines_deleted INT COMMENT '删除行数',
    commit_message TEXT COMMENT '提交信息',
    files_changed INT COMMENT '修改文件数',
    
    -- Pull Request相关字段
    pr_number INT COMMENT 'PR编号',
    pr_title TEXT COMMENT 'PR标题',
    pr_state VARCHAR(20) COMMENT 'PR状态',
    pr_merged_at TIMESTAMP NULL COMMENT 'PR合并时间',
    
    -- Issue相关字段
    issue_number INT COMMENT 'Issue编号',
    issue_title TEXT COMMENT 'Issue标题',
    issue_state VARCHAR(20) COMMENT 'Issue状态',
    issue_closed_at TIMESTAMP NULL COMMENT 'Issue关闭时间',
    
    -- Release相关字段
    release_tag VARCHAR(100) COMMENT 'Release标签',
    release_name TEXT COMMENT 'Release名称',
    release_published_at TIMESTAMP NULL COMMENT 'Release发布时间',
    is_prerelease BOOLEAN COMMENT '是否为预发布版本',
    
    -- 分支和标签相关字段
    branch_name VARCHAR(255) COMMENT '分支名称',
    tag_name VARCHAR(100) COMMENT '标签名称',
    
    -- 统计相关字段
    total_commits INT COMMENT '总提交数',
    owner_commits INT COMMENT '拥有者提交数',
    other_commits INT COMMENT '其他提交数',
    weekly_commits INT COMMENT '周提交数',
    additions_count INT COMMENT '新增行数统计',
    deletions_count INT COMMENT '删除行数统计',
    
    -- 作者信息
    author_login VARCHAR(100) COMMENT '作者登录名',
    author_name VARCHAR(255) COMMENT '作者姓名',
    author_email VARCHAR(255) COMMENT '作者邮箱',
    
    -- 通用字段
    metadata JSON COMMENT '其他元数据(JSON格式)',
    activity_time TIMESTAMP NOT NULL COMMENT '活动时间',
    created_at TIMESTAMP NOT NULL COMMENT '创建时间',
    
    INDEX idx_repository_id (repository_id),
    INDEX idx_member_id (member_id),
    INDEX idx_activity_type (activity_type),
    INDEX idx_activity_time (activity_time),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Git活动记录表';

-- 7. 贡献度评分表
CREATE TABLE IF NOT EXISTS contribution_scores (
    score_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '评分ID',
    course_id INT NOT NULL COMMENT '课程ID',
    member_id INT NOT NULL COMMENT '成员ID',
    total_score DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总分',
    dimension_scores JSON NOT NULL COMMENT '各维度得分(JSON格式)',
    ranking INT COMMENT '排名',
    calculation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '计算时间',
    config_id INT COMMENT '使用的配置ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_course_member (course_id, member_id),
    INDEX idx_course_id (course_id),
    INDEX idx_member_id (member_id),
    INDEX idx_total_score (total_score),
    INDEX idx_ranking (ranking),
    INDEX idx_calculation_time (calculation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='贡献度评分表';

-- 添加外键约束
ALTER TABLE course_enrollments 
ADD CONSTRAINT fk_enrollment_course 
FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE;

ALTER TABLE evaluation_configs 
ADD CONSTRAINT fk_config_course 
FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE;

ALTER TABLE git_repositories 
ADD CONSTRAINT fk_repository_course 
FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE;

ALTER TABLE git_activities 
ADD CONSTRAINT fk_activity_repository 
FOREIGN KEY (repository_id) REFERENCES git_repositories(repository_id) ON DELETE CASCADE;

ALTER TABLE contribution_scores 
ADD CONSTRAINT fk_score_course 
FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE;

ALTER TABLE github_config 
ADD CONSTRAINT fk_github_config_course 
FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE;

ALTER TABLE contribution_scores 
ADD CONSTRAINT fk_score_config 
FOREIGN KEY (config_id) REFERENCES evaluation_configs(config_id) ON DELETE SET NULL;

-- 插入初始化数据

-- 1. 插入测试课程数据
INSERT INTO courses (organization_id, course_name, course_description, teacher_id, start_date, end_date, status) VALUES
(1, 'Java程序设计', 'Java编程语言基础与实践课程', 1001, '2024-02-01', '2024-06-30', 'active'),
(1, 'Web开发技术', '前端与后端Web开发技术综合课程', 1002, '2024-02-01', '2024-06-30', 'active'),
(2, '数据结构与算法', '计算机科学核心课程，涵盖基础数据结构和算法', 1003, '2024-02-01', '2024-06-30', 'active');

-- 2. 插入课程注册数据
INSERT INTO course_enrollments (course_id, member_id, member_name, member_email, student_id, group_name, status) VALUES
(1, 2001, '张三', 'zhangsan@example.com', '2021001', 'Group A', 'active'),
(1, 2002, '李四', 'lisi@example.com', '2021002', 'Group A', 'active'),
(1, 2003, '王五', 'wangwu@example.com', '2021003', 'Group B', 'active'),
(2, 2001, '张三', 'zhangsan@example.com', '2021001', 'Team 1', 'active'),
(2, 2004, '赵六', 'zhaoliu@example.com', '2021004', 'Team 1', 'active'),
(3, 2002, '李四', 'lisi@example.com', '2021002', 'Alpha', 'active');

-- 3. 插入评价配置数据
INSERT INTO evaluation_configs (course_id, config_name, dimension_weights, calculation_method, created_by) VALUES
(1, '默认评价配置', '{"commit_frequency": 0.25, "code_quality": 0.30, "collaboration": 0.20, "innovation": 0.15, "documentation": 0.10}', 'weighted_average', 1001),
(2, 'Web项目评价配置', '{"commit_frequency": 0.20, "code_quality": 0.35, "collaboration": 0.25, "innovation": 0.10, "documentation": 0.10}', 'weighted_average', 1002),
(3, '算法课程评价配置', '{"commit_frequency": 0.15, "code_quality": 0.40, "collaboration": 0.15, "innovation": 0.20, "documentation": 0.10}', 'weighted_average', 1003);

-- 4. 插入Git仓库数据
INSERT INTO git_repositories (course_id, repository_name, repository_url, platform, branch_name, is_active) VALUES
(1, 'java-course-project', 'https://github.com/example/java-course-project', 'github', 'main', TRUE),
(1, 'java-homework', 'https://github.com/example/java-homework', 'github', 'main', TRUE),
(2, 'web-development-project', 'https://github.com/example/web-dev-project', 'github', 'main', TRUE),
(3, 'algorithm-implementations', 'https://github.com/example/algorithms', 'github', 'main', TRUE);

-- 5. 插入Git活动记录数据
INSERT INTO git_activities (repository_id, member_id, activity_type, commit_hash, commit_message, files_changed, lines_added, lines_deleted, activity_time) VALUES
(1, 2001, 'commit', 'abc123def456', '初始化项目结构', 5, 120, 0, '2024-02-15 10:30:00'),
(1, 2001, 'commit', 'def456ghi789', '添加用户管理模块', 3, 85, 10, '2024-02-16 14:20:00'),
(1, 2002, 'commit', 'ghi789jkl012', '实现登录功能', 2, 65, 5, '2024-02-17 09:15:00'),
(1, 2003, 'commit', 'jkl012mno345', '添加单元测试', 4, 95, 0, '2024-02-18 16:45:00'),
(2, 2001, 'commit', 'mno345pqr678', '创建前端页面', 6, 150, 0, '2024-02-20 11:00:00'),
(2, 2004, 'commit', 'pqr678stu901', '实现API接口', 3, 78, 12, '2024-02-21 13:30:00');

-- 6. 插入贡献度评分数据
INSERT INTO contribution_scores (course_id, member_id, total_score, dimension_scores, ranking, config_id) VALUES
(1, 2001, 85.50, '{"commit_frequency": 22.5, "code_quality": 26.0, "collaboration": 18.0, "innovation": 12.0, "documentation": 7.0}', 1, 1),
(1, 2002, 78.25, '{"commit_frequency": 20.0, "code_quality": 24.5, "collaboration": 16.5, "innovation": 10.5, "documentation": 6.75}', 2, 1),
(1, 2003, 72.80, '{"commit_frequency": 18.5, "code_quality": 22.0, "collaboration": 15.0, "innovation": 9.5, "documentation": 7.8}', 3, 1),
(2, 2001, 88.75, '{"commit_frequency": 18.0, "code_quality": 31.5, "collaboration": 22.0, "innovation": 8.5, "documentation": 8.75}', 1, 2),
(2, 2004, 76.50, '{"commit_frequency": 16.0, "code_quality": 28.0, "collaboration": 19.0, "innovation": 7.0, "documentation": 6.5}', 2, 2);

-- 创建视图：课程成员贡献度排行榜
CREATE OR REPLACE VIEW course_leaderboard AS
SELECT 
    cs.course_id,
    c.course_name,
    cs.member_id,
    ce.member_name,
    ce.student_id,
    ce.group_name,
    cs.total_score,
    cs.ranking,
    cs.calculation_time
FROM contribution_scores cs
JOIN courses c ON cs.course_id = c.course_id
JOIN course_enrollments ce ON cs.course_id = ce.course_id AND cs.member_id = ce.member_id
WHERE ce.status = 'active'
ORDER BY cs.course_id, cs.ranking;

-- 创建视图：Git活动统计
CREATE OR REPLACE VIEW git_activity_stats AS
SELECT 
    ga.repository_id,
    gr.repository_name,
    gr.course_id,
    ga.member_id,
    ce.member_name,
    COUNT(*) as total_activities,
    SUM(CASE WHEN ga.activity_type = 'commit' THEN 1 ELSE 0 END) as commit_count,
    SUM(ga.lines_added) as total_lines_added,
    SUM(ga.lines_deleted) as total_lines_deleted,
    SUM(ga.files_changed) as total_files_changed,
    MIN(ga.activity_time) as first_activity,
    MAX(ga.activity_time) as last_activity
FROM git_activities ga
JOIN git_repositories gr ON ga.repository_id = gr.repository_id
JOIN course_enrollments ce ON gr.course_id = ce.course_id AND ga.member_id = ce.member_id
WHERE gr.is_active = TRUE AND ce.status = 'active'
GROUP BY ga.repository_id, ga.member_id;

-- 创建存储过程：重新计算课程排名
DELIMITER //
CREATE PROCEDURE RecalculateCourseRanking(IN p_course_id INT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_member_id INT;
    DECLARE v_rank INT DEFAULT 1;
    
    DECLARE cur CURSOR FOR 
        SELECT member_id 
        FROM contribution_scores 
        WHERE course_id = p_course_id 
        ORDER BY total_score DESC;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO v_member_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        UPDATE contribution_scores 
        SET ranking = v_rank 
        WHERE course_id = p_course_id AND member_id = v_member_id;
        
        SET v_rank = v_rank + 1;
    END LOOP;
    
    CLOSE cur;
END //
DELIMITER ;

-- 创建触发器：更新贡献度评分时自动更新时间
DELIMITER //
CREATE TRIGGER tr_contribution_scores_update 
    BEFORE UPDATE ON contribution_scores
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END //
DELIMITER ;

-- 创建索引优化查询性能
CREATE INDEX idx_git_activities_composite ON git_activities(repository_id, member_id, activity_time);
CREATE INDEX idx_contribution_scores_composite ON contribution_scores(course_id, total_score DESC, ranking);
CREATE INDEX idx_course_enrollments_composite ON course_enrollments(course_id, status, group_name);

-- 数据库表结构创建完成
-- 注意：在实际部署时，请根据具体需求调整表结构和初始化数据