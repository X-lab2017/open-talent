package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.EvaluationConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 评价配置Mapper接口
 */
@Mapper
public interface EvaluationConfigMapper extends BaseMapper<EvaluationConfig> {
    
    /**
     * 根据课程ID查询评价配置
     * @param courseId 课程ID
     * @return 评价配置
     */
    @Select("SELECT * FROM evaluation_configs WHERE course_id = #{courseId}")
    EvaluationConfig selectByCourseId(@Param("courseId") Integer courseId);
}