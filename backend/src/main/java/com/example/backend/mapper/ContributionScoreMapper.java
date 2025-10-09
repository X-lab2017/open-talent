package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.ContributionScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

/**
 * 贡献度评分Mapper接口
 */
@Mapper
public interface ContributionScoreMapper extends BaseMapper<ContributionScore> {
    
    /**
     * 根据课程ID分页查询贡献度评分（按排名排序）
     * @param page 分页对象
     * @param courseId 课程ID
     * @return 贡献度评分分页数据
     */
    @Select("SELECT * FROM contribution_scores WHERE course_id = #{courseId} ORDER BY ranking ASC")
    IPage<ContributionScore> selectByCourseIdOrderByRanking(Page<ContributionScore> page, @Param("courseId") Integer courseId);
    
    /**
     * 根据课程ID查询贡献度评分（按总分降序排序）
     * @param courseId 课程ID
     * @return 贡献度评分列表
     */
    @Select("SELECT * FROM contribution_scores WHERE course_id = #{courseId} ORDER BY total_score DESC")
    List<ContributionScore> selectByCourseIdOrderByScore(@Param("courseId") Integer courseId);
    
    /**
     * 根据课程ID和成员ID查询贡献度评分
     * @param courseId 课程ID
     * @param memberId 成员ID
     * @return 贡献度评分
     */
    @Select("SELECT * FROM contribution_scores WHERE course_id = #{courseId} AND member_id = #{memberId} ORDER BY calculation_time DESC LIMIT 1")
    ContributionScore selectByCourseIdAndMemberId(@Param("courseId") Integer courseId, @Param("memberId") Integer memberId);
    
    /**
     * 根据成员ID查询所有课程的贡献度评分
     * @param memberId 成员ID
     * @return 贡献度评分列表
     */
    @Select("SELECT * FROM contribution_scores WHERE member_id = #{memberId} ORDER BY calculation_time DESC")
    List<ContributionScore> selectByMemberId(@Param("memberId") Integer memberId);
    
    /**
     * 根据课程ID查询前N名学生
     * @param courseId 课程ID
     * @param limit 限制数量
     * @return 贡献度评分列表
     */
    @Select("SELECT * FROM contribution_scores WHERE course_id = #{courseId} ORDER BY total_score DESC LIMIT #{limit}")
    List<ContributionScore> selectTopNByCourseId(@Param("courseId") Integer courseId, @Param("limit") Integer limit);
    
    /**
     * 根据课程ID删除所有评分记录（重新计算前清理）
     * @param courseId 课程ID
     * @return 删除数量
     */
    @Delete("DELETE FROM contribution_scores WHERE course_id = #{courseId}")
    int deleteByCourseId(@Param("courseId") Integer courseId);
    
    /**
     * 根据课程ID统计参与评分的学生数量
     * @param courseId 课程ID
     * @return 学生数量
     */
    @Select("SELECT COUNT(DISTINCT member_id) FROM contribution_scores WHERE course_id = #{courseId}")
    Long countStudentsByCourseId(@Param("courseId") Integer courseId);
}