package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.GitHubConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * GitHub配置Mapper接口
 */
@Mapper
public interface GitHubConfigMapper extends BaseMapper<GitHubConfig> {
    
    /**
     * 根据课程ID查询GitHub配置
     * @param courseId 课程ID
     * @return GitHub配置
     */
    @Select("SELECT gc.*, c.course_name as courseName FROM github_config gc " +
            "LEFT JOIN courses c ON gc.course_id = c.course_id " +
            "WHERE gc.course_id = #{courseId}")
    GitHubConfig selectByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 查询所有GitHub配置（包含课程名称）
     * @return GitHub配置列表
     */
    @Select("SELECT gc.*, c.course_name as courseName FROM github_config gc " +
            "LEFT JOIN courses c ON gc.course_id = c.course_id " +
            "ORDER BY gc.created_at DESC")
    List<GitHubConfig> selectAllWithCourseName();
    
    /**
     * 更新Token状态
     * @param courseId 课程ID
     * @param tokenStatus Token状态
     * @return 更新数量
     */
    @Update("UPDATE github_config SET token_status = #{tokenStatus}, updated_at = NOW() WHERE course_id = #{courseId}")
    int updateTokenStatus(@Param("courseId") Long courseId, @Param("tokenStatus") String tokenStatus);
    
    /**
     * 根据课程ID删除GitHub配置
     * @param courseId 课程ID
     * @return 删除数量
     */
    @Update("DELETE FROM github_config WHERE course_id = #{courseId}")
    int deleteByCourseId(@Param("courseId") Long courseId);
}