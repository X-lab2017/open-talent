package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程注册实体类
 */
@Data
@TableName("course_enrollments")
public class CourseEnrollment {
    
    /**
     * 注册ID
     */
    @TableId(value = "enrollment_id", type = IdType.AUTO)
    private Integer enrollmentId;
    
    /**
     * 课程ID
     */
    @TableField("course_id")
    private Integer courseId;
    
    /**
     * 成员ID
     */
    @TableField("member_id")
    private Integer memberId;
    
    /**
     * 成员姓名
     */
    @TableField("member_name")
    private String memberName;
    
    /**
     * 成员邮箱
     */
    @TableField("member_email")
    private String memberEmail;
    
    /**
     * 学号
     */
    @TableField("student_id")
    private String studentId;
    
    /**
     * 分组名称
     */
    @TableField("group_name")
    private String groupName;
    
    /**
     * 注册时间
     */
    @TableField("enrollment_date")
    private LocalDateTime enrollmentDate;
    
    /**
     * 状态：active-活跃, inactive-非活跃, dropped-已退出
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
}