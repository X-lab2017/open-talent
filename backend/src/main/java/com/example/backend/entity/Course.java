package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 课程实体类
 */
@Data
@TableName("courses")
public class Course {
    
    /**
     * 课程ID
     */
    @TableId(value = "course_id", type = IdType.AUTO)
    private Integer courseId;
    
    /**
     * 组织ID
     */
    @TableField("organization_id")
    private Integer organizationId;
    
    /**
     * 课程名称
     */
    @TableField("course_name")
    private String courseName;
    
    /**
     * 课程描述
     */
    @TableField("course_description")
    private String courseDescription;
    
    /**
     * 教师ID
     */
    @TableField("teacher_id")
    private Integer teacherId;
    
    /**
     * 开始日期
     */
    @TableField("start_date")
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    @TableField("end_date")
    private LocalDate endDate;
    
    /**
     * 课程状态：active-活跃, inactive-非活跃, archived-已归档
     */
    @TableField("status")
    private String status;
    
    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 学生数量（非数据库字段，用于返回给前端）
     */
    @TableField(exist = false)
    private Integer studentCount;
}