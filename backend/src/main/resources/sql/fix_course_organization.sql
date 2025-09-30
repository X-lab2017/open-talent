-- 修复课程组织数据一致性问题
-- 更新时间: 2025-01-27
-- 描述: 将课程的organization_id更新为与实际成员数据匹配的组织ID

-- 更新课程2的组织ID从1改为3，匹配组织3下的成员（张三、李四、陈七、孙九）
UPDATE courses SET organization_id = 3 WHERE course_id = 2;

-- 更新课程3的组织ID从2改为4，匹配组织4下的成员（王五、刘八）
UPDATE courses SET organization_id = 4 WHERE course_id = 3;

-- 验证更新结果
SELECT course_id, course_name, organization_id FROM courses WHERE course_id IN (2, 3);