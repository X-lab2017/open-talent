package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.GitActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Git活动记录Mapper接口
 */
@Mapper
public interface GitActivityMapper extends BaseMapper<GitActivity> {
    
    /**
     * 根据仓库ID分页查询Git活动记录
     * @param page 分页对象
     * @param repoId 仓库ID
     * @return Git活动记录分页数据
     */
    @Select("SELECT * FROM git_activities WHERE repository_id = #{repoId} ORDER BY activity_time DESC")
    IPage<GitActivity> selectByRepoId(Page<GitActivity> page, @Param("repoId") Integer repoId);
    
    /**
     * 根据成员ID查询Git活动记录
     * @param memberId 成员ID
     * @return Git活动记录列表
     */
    @Select("SELECT * FROM git_activities WHERE member_id = #{memberId} ORDER BY activity_time DESC")
    List<GitActivity> selectByMemberId(@Param("memberId") Integer memberId);
    
    /**
     * 根据成员ID和时间范围查询Git活动记录
     * @param memberId 成员ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return Git活动记录列表
     */
    @Select("SELECT * FROM git_activities WHERE member_id = #{memberId} AND activity_time BETWEEN #{startTime} AND #{endTime} ORDER BY activity_time DESC")
    List<GitActivity> selectByMemberIdAndTimeRange(@Param("memberId") Integer memberId, 
                                                   @Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据仓库ID和成员ID查询Git活动记录
     * @param repoId 仓库ID
     * @param memberId 成员ID
     * @return Git活动记录列表
     */
    @Select("SELECT * FROM git_activities WHERE repository_id = #{repoId} AND member_id = #{memberId} ORDER BY activity_time DESC")
    List<GitActivity> selectByRepoIdAndMemberId(@Param("repoId") Integer repoId, @Param("memberId") Integer memberId);
    
    /**
     * 根据活动类型查询Git活动记录
     * @param repoId 仓库ID
     * @param activityType 活动类型
     * @return Git活动记录列表
     */
    @Select("SELECT * FROM git_activities WHERE repository_id = #{repoId} AND activity_type = #{activityType} ORDER BY activity_time DESC")
    List<GitActivity> selectByRepoIdAndActivityType(@Param("repoId") Integer repoId, @Param("activityType") String activityType);
    
    /**
     * 根据时间范围查询Git活动记录
     * @param repoId 仓库ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return Git活动记录列表
     */
    @Select("SELECT * FROM git_activities WHERE repository_id = #{repoId} AND activity_time BETWEEN #{startTime} AND #{endTime} ORDER BY activity_time DESC")
    List<GitActivity> selectByRepoIdAndTimeRange(@Param("repoId") Integer repoId, 
                                                @Param("startTime") LocalDateTime startTime, 
                                                @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据仓库ID和成员ID统计活动数量
     * @param repoId 仓库ID
     * @param memberId 成员ID
     * @return 活动数量
     */
    @Select("SELECT COUNT(*) FROM git_activities WHERE repository_id = #{repoId} AND member_id = #{memberId}")
    Long countByRepoIdAndMemberId(@Param("repoId") Integer repoId, @Param("memberId") Integer memberId);
    
    /**
     * 根据仓库ID和成员ID统计代码行数
     * @param repoId 仓库ID
     * @param memberId 成员ID
     * @return 代码行数统计结果 [总新增行数, 总删除行数]
     */
    @Select("SELECT COALESCE(SUM(lines_added), 0) as total_added, COALESCE(SUM(lines_deleted), 0) as total_deleted " +
            "FROM git_activities WHERE repository_id = #{repoId} AND member_id = #{memberId} AND activity_type = 'commit'")
    List<Object[]> countLinesByRepoIdAndMemberId(@Param("repoId") Integer repoId, @Param("memberId") Integer memberId);
    
    /**
     * 获取成员在指定时间段内的提交统计
     * @param memberId 成员ID
     * @param repoId 仓库ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 提交统计数据
     */
    @Select("SELECT activity_id, repository_id, member_id, activity_type, activity_time, " +
            "COALESCE(SUM(lines_added), 0) as lines_added, COALESCE(SUM(lines_deleted), 0) as lines_deleted, " +
            "COUNT(*) as commit_count, created_at " +
            "FROM git_activities WHERE member_id = #{memberId} AND repository_id = #{repoId} " +
            "AND activity_time BETWEEN #{startTime} AND #{endTime} AND activity_type = 'commit' " +
            "GROUP BY member_id, repository_id LIMIT 1")
    GitActivity selectMemberCommitStats(@Param("memberId") Integer memberId, 
                                        @Param("repoId") Integer repoId,
                                        @Param("startTime") LocalDateTime startTime, 
                                        @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据提交哈希值查询Git活动记录
     * @param commitHash 提交哈希值
     * @return Git活动记录列表
     */
    @Select("SELECT * FROM git_activities WHERE commit_hash = #{commitHash}")
    List<GitActivity> selectByCommitHash(@Param("commitHash") String commitHash);
    
    /**
     * 查询未关联到系统成员的Git活动记录（member_id为null）
     * @param repoId 仓库ID
     * @return Git活动记录列表
     */
    @Select("SELECT * FROM git_activities WHERE repository_id = #{repoId} AND member_id IS NULL ORDER BY activity_time DESC")
    List<GitActivity> selectUnlinkedActivitiesByRepoId(@Param("repoId") Integer repoId);
    
    /**
     * 统计未关联到系统成员的Git活动数量
     * @param repoId 仓库ID
     * @return 未关联活动数量
     */
    @Select("SELECT COUNT(*) FROM git_activities WHERE repository_id = #{repoId} AND member_id IS NULL")
    Long countUnlinkedActivitiesByRepoId(@Param("repoId") Integer repoId);
    
    /**
     * 根据作者名称查询Git活动记录（包括未关联的）
     * @param authorName 作者名称
     * @return Git活动记录列表
     */
    @Select("SELECT * FROM git_activities WHERE author_name = #{authorName} ORDER BY activity_time DESC")
    List<GitActivity> selectByAuthorName(@Param("authorName") String authorName);
    
    /**
     * 获取未关联用户的统计信息
     * @param repositoryId 仓库ID
     * @return 未关联用户统计信息列表
     */
    @Select("SELECT author_name, COUNT(*) as activity_count FROM git_activities WHERE repository_id = #{repositoryId} AND member_id IS NULL GROUP BY author_name")
    @Results({
        @Result(column = "author_name", property = "authorName"),
        @Result(column = "activity_count", property = "activityCount")
    })
    List<java.util.Map<String, Object>> getUnlinkedUserStats(@Param("repositoryId") Integer repositoryId);
    
    /**
     * 检查是否存在相同的activity记录（用于去重）
     * @param repositoryId 仓库ID
     * @param activityType 活动类型
     * @param metadata 元数据（用于唯一性判断）
     * @return 存在的记录数量
     */
    @Select("SELECT COUNT(*) FROM git_activities WHERE repository_id = #{repositoryId} AND activity_type = #{activityType} AND metadata = #{metadata}")
    Long countByRepoIdAndTypeAndMetadata(@Param("repositoryId") Long repositoryId, 
                                        @Param("activityType") String activityType, 
                                        @Param("metadata") String metadata);
    
    /**
     * 检查是否存在相同的commit记录（用于去重）
     * @param repositoryId 仓库ID
     * @param commitHash 提交哈希值
     * @return 存在的记录数量
     */
    @Select("SELECT COUNT(*) FROM git_activities WHERE repository_id = #{repositoryId} AND commit_hash = #{commitHash}")
    Long countByRepoIdAndCommitHash(@Param("repositoryId") Long repositoryId, 
                                   @Param("commitHash") String commitHash);
    
    /**
     * 删除指定仓库的特定类型活动记录（用于重新同步前清理）
     * @param repositoryId 仓库ID
     * @param activityType 活动类型
     * @return 删除的记录数量
     */
    @Select("DELETE FROM git_activities WHERE repository_id = #{repositoryId} AND activity_type = #{activityType}")
    int deleteByRepoIdAndActivityType(@Param("repositoryId") Long repositoryId, 
                                     @Param("activityType") String activityType);
}