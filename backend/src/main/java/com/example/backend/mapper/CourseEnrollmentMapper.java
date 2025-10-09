package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.CourseEnrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 课程注册Mapper接口
 */
@Mapper
public interface CourseEnrollmentMapper extends BaseMapper<CourseEnrollment> {
    
    /**
     * 根据课程ID查询所有注册学生
     * @param courseId 课程ID
     * @return 注册记录列表
     */
    @Select("SELECT * FROM course_enrollments WHERE course_id = #{courseId} ORDER BY enrollment_date DESC")
    List<CourseEnrollment> selectByCourseId(@Param("courseId") Integer courseId);
    
    /**
     * 根据成员ID查询所有注册课程
     * @param memberId 成员ID
     * @return 注册记录列表
     */
    @Select("SELECT * FROM course_enrollments WHERE member_id = #{memberId} ORDER BY enrollment_date DESC")
    List<CourseEnrollment> selectByMemberId(@Param("memberId") Integer memberId);
    
    /**
     * 根据课程ID和角色查询注册记录
     * @param courseId 课程ID
     * @param role 角色
     * @return 注册记录列表
     */
    @Select("SELECT * FROM course_enrollments WHERE course_id = #{courseId} AND role = #{role} ORDER BY enrollment_date DESC")
    List<CourseEnrollment> selectByCourseIdAndRole(@Param("courseId") Integer courseId, @Param("role") String role);
    
    /**
     * 根据课程ID和分组名称查询注册记录
     * @param courseId 课程ID
     * @param groupName 分组名称
     * @return 注册记录列表
     */
    @Select("SELECT * FROM course_enrollments WHERE course_id = #{courseId} AND group_name = #{groupName} ORDER BY enrollment_date DESC")
    List<CourseEnrollment> selectByCourseIdAndGroupName(@Param("courseId") Integer courseId, @Param("groupName") String groupName);
    
    /**
     * 检查学生是否已注册课程
     * @param courseId 课程ID
     * @param memberId 成员ID
     * @return 注册记录
     */
    @Select("SELECT * FROM course_enrollments WHERE course_id = #{courseId} AND member_id = #{memberId}")
    CourseEnrollment selectByCourseIdAndMemberId(@Param("courseId") Integer courseId, @Param("memberId") Integer memberId);
    
    /**
     * 批量删除课程注册记录
     * @param courseId 课程ID
     * @param memberIds 成员ID列表
     * @return 删除数量
     */
    @Delete("DELETE FROM course_enrollments WHERE course_id = #{courseId} AND member_id IN (${memberIds})")
    int deleteByCourseIdAndMemberIds(@Param("courseId") Integer courseId, @Param("memberIds") String memberIds);
}