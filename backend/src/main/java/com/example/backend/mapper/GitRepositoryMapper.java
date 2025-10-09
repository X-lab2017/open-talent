package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.GitRepository;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Git仓库Mapper接口
 */
@Mapper
public interface GitRepositoryMapper extends BaseMapper<GitRepository> {
    
    /**
     * 根据课程ID查询Git仓库列表（包含课程名称）
     * @param courseId 课程ID
     * @return Git仓库列表
     */
    @Select("SELECT gr.*, c.course_name as courseName FROM git_repositories gr " +
            "LEFT JOIN courses c ON gr.course_id = c.course_id " +
            "WHERE gr.course_id = #{courseId} ORDER BY gr.created_at DESC")
    List<GitRepository> selectByCourseId(@Param("courseId") Integer courseId);
    
    /**
     * 根据平台类型查询Git仓库列表
     * @param courseId 课程ID
     * @param platform 平台类型
     * @return Git仓库列表
     */
    @Select("SELECT * FROM git_repositories WHERE course_id = #{courseId} AND platform = #{platform} ORDER BY created_at DESC")
    List<GitRepository> selectByCourseIdAndPlatform(@Param("courseId") Integer courseId, @Param("platform") String platform);
    
    /**
     * 更新最后同步时间
     * @param repoId 仓库ID
     * @param lastSyncAt 最后同步时间
     * @return 更新数量
     */
    @Update("UPDATE git_repositories SET last_sync_time = #{lastSyncAt} WHERE repository_id = #{repoId}")
    int updateLastSyncAt(@Param("repoId") Integer repoId, @Param("lastSyncAt") LocalDateTime lastSyncAt);
    
    /**
     * 根据仓库URL查询Git仓库
     * @param repoUrl 仓库URL
     * @return Git仓库
     */
    @Select("SELECT * FROM git_repositories WHERE repo_url = #{repoUrl}")
    GitRepository selectByRepoUrl(@Param("repoUrl") String repoUrl);
}