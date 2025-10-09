package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程Mapper接口
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    
    /**
     * 根据组织ID分页查询课程
     * @param page 分页对象
     * @param organizationId 组织ID
     * @return 课程分页数据
     */
    @Select("SELECT * FROM courses WHERE organization_id = #{organizationId} ORDER BY created_at DESC")
    IPage<Course> selectByOrganizationId(Page<Course> page, @Param("organizationId") Integer organizationId);
    
    /**
     * 根据教师ID查询课程列表
     * @param teacherId 教师ID
     * @return 课程列表
     */
    @Select("SELECT * FROM courses WHERE teacher_id = #{teacherId} ORDER BY created_at DESC")
    List<Course> selectByTeacherId(@Param("teacherId") Integer teacherId);
    
    /**
     * 根据状态查询课程列表
     * @param organizationId 组织ID
     * @param status 课程状态
     * @return 课程列表
     */
    @Select("SELECT * FROM courses WHERE organization_id = #{organizationId} AND status = #{status} ORDER BY created_at DESC")
    List<Course> selectByStatus(@Param("organizationId") Integer organizationId, @Param("status") String status);
}