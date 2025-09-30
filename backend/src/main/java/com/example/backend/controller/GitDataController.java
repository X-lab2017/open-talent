package com.example.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.common.Result;
import com.example.backend.entity.GitActivity;
import com.example.backend.entity.GitRepository;
import com.example.backend.service.GitDataService;
import com.example.backend.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Git数据采集控制器
 */
@Api(tags = "Git数据采集管理")
@RestController
@RequestMapping("/git-data")
@CrossOrigin
public class GitDataController {
    
    @Autowired
    private GitDataService gitDataService;
    
    /**
     * 添加Git仓库
     */
    @ApiOperation("添加Git仓库")
    @PostMapping("/repositories")
    public Result<Boolean> addGitRepository(@Valid @RequestBody GitRepository gitRepository) {
        System.out.println("[DEBUG] GitDataController.addGitRepository - 接收到添加仓库请求");
        System.out.println("[DEBUG] GitDataController.addGitRepository - 请求参数: " + gitRepository);
        
        try {
            // 验证必要字段
            if (gitRepository.getRepositoryName() == null || gitRepository.getRepositoryName().trim().isEmpty()) {
                System.out.println("[DEBUG] GitDataController.addGitRepository - 仓库名称为空");
                return Result.error("仓库名称不能为空");
            }
            
            if (gitRepository.getRepositoryUrl() == null || gitRepository.getRepositoryUrl().trim().isEmpty()) {
                System.out.println("[DEBUG] GitDataController.addGitRepository - 仓库URL为空");
                return Result.error("仓库URL不能为空");
            }
            
            if (gitRepository.getCourseId() == null) {
                System.out.println("[DEBUG] GitDataController.addGitRepository - 课程ID为空");
                return Result.error("课程ID不能为空");
            }
            
            // 检查该课程是否已经存在仓库
            List<GitRepository> existingRepositories = gitDataService.getRepositoriesByCourseId(gitRepository.getCourseId());
            if (existingRepositories != null && !existingRepositories.isEmpty()) {
                System.out.println("[DEBUG] GitDataController.addGitRepository - 课程ID " + gitRepository.getCourseId() + " 已存在仓库，不允许重复添加");
                return Result.error("该课程已存在Git仓库，每门课程只能添加一个仓库");
            }
            
            System.out.println("[DEBUG] GitDataController.addGitRepository - 参数验证通过，调用服务层添加仓库");
            System.out.println("[DEBUG] GitDataController.addGitRepository - 仓库名称: " + gitRepository.getRepositoryName());
            System.out.println("[DEBUG] GitDataController.addGitRepository - 仓库URL: " + gitRepository.getRepositoryUrl());
            System.out.println("[DEBUG] GitDataController.addGitRepository - 课程ID: " + gitRepository.getCourseId());
            
            boolean success = gitDataService.addGitRepository(gitRepository);
            
            System.out.println("[DEBUG] GitDataController.addGitRepository - 服务层执行结果: " + success);
            
            if (success) {
                System.out.println("[DEBUG] GitDataController.addGitRepository - 仓库添加成功");
                Result<Boolean> result = Result.success(true);
                System.out.println("[DEBUG] GitDataController.addGitRepository - 返回成功结果: " + result);
                return result;
            } else {
                System.out.println("[DEBUG] GitDataController.addGitRepository - 仓库添加失败，服务层返回false");
                return Result.error("Git仓库添加失败");
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] GitDataController.addGitRepository - 发生异常: " + e.getClass().getSimpleName());
            System.out.println("[DEBUG] GitDataController.addGitRepository - 异常消息: " + e.getMessage());
            System.out.println("[DEBUG] GitDataController.addGitRepository - 异常堆栈跟踪:");
            e.printStackTrace();
            
            // 根据异常类型返回不同的错误信息
            if (e instanceof org.springframework.dao.DuplicateKeyException) {
                System.out.println("[DEBUG] GitDataController.addGitRepository - 检测到重复键异常，可能是仓库URL已存在");
                return Result.error("仓库URL已存在，请检查后重试");
            } else if (e instanceof org.springframework.dao.DataIntegrityViolationException) {
                System.out.println("[DEBUG] GitDataController.addGitRepository - 检测到数据完整性违反异常");
                return Result.error("数据完整性错误，请检查输入参数");
            } else {
                System.out.println("[DEBUG] GitDataController.addGitRepository - 其他类型异常");
                return Result.error("Git仓库添加失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 更新Git仓库信息
     */
    @ApiOperation("更新Git仓库信息")
    @PutMapping("/repositories/{repositoryId}")
    public Result<Boolean> updateGitRepository(
            @ApiParam("仓库ID") @PathVariable Integer repositoryId,
            @Valid @RequestBody GitRepository gitRepository) {
        try {
            gitRepository.setRepositoryId(repositoryId);
            boolean success = gitDataService.updateGitRepository(gitRepository);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("Git仓库更新失败");
            }
        } catch (Exception e) {
            return Result.error("Git仓库更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除Git仓库
     */
    @ApiOperation("删除Git仓库")
    @DeleteMapping("/repositories/{repositoryId}")
    public Result<Boolean> deleteGitRepository(@ApiParam("仓库ID") @PathVariable Integer repositoryId) {
        try {
            boolean success = gitDataService.deleteGitRepository(repositoryId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("Git仓库删除失败");
            }
        } catch (Exception e) {
            return Result.error("Git仓库删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据课程ID获取Git仓库列表
     */
    @ApiOperation("根据课程ID获取Git仓库列表")
    @GetMapping("/repositories/course/{courseId}")
    public Result<List<GitRepository>> getRepositoriesByCourseId(@ApiParam("课程ID") @PathVariable Integer courseId) {
        try {
            List<GitRepository> repositories = gitDataService.getRepositoriesByCourseId(courseId);
            return Result.success(repositories);
        } catch (Exception e) {
            return Result.error("查询Git仓库列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据课程ID获取Git仓库列表 - 支持查询参数格式
     */
    @ApiOperation("根据课程ID获取Git仓库列表（查询参数）")
    @GetMapping("/repositories")
    public Result<List<GitRepository>> getRepositoriesByCourseIdParam(@ApiParam("课程ID") @RequestParam Integer courseId) {
        try {
            // 调试信息：打印接收到的courseId参数
            System.out.println("[DEBUG] GitDataController.getRepositoriesByCourseIdParam - 接收到的courseId: " + courseId);
            
            List<GitRepository> repositories = gitDataService.getRepositoriesByCourseId(courseId);
            
            // 调试信息：打印查询结果
            System.out.println("[DEBUG] GitDataController.getRepositoriesByCourseIdParam - 查询到的仓库数量: " + (repositories != null ? repositories.size() : 0));
            if (repositories != null && !repositories.isEmpty()) {
                System.out.println("[DEBUG] GitDataController.getRepositoriesByCourseIdParam - 第一个仓库信息: " + repositories.get(0));
            }
            
            Result<List<GitRepository>> result = Result.success(repositories);
            
            // 调试信息：打印返回的数据
            System.out.println("[DEBUG] GitDataController.getRepositoriesByCourseIdParam - 返回结果: " + result);
            
            return result;
        } catch (Exception e) {
            System.out.println("[DEBUG] GitDataController.getRepositoriesByCourseIdParam - 发生异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("查询Git仓库列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据平台类型获取Git仓库列表
     */
    @ApiOperation("根据平台类型获取Git仓库列表")
    @GetMapping("/repositories/course/{courseId}/platform/{platform}")
    public Result<List<GitRepository>> getRepositoriesByPlatform(
            @ApiParam("课程ID") @PathVariable Integer courseId,
            @ApiParam("平台类型") @PathVariable String platform) {
        try {
            List<GitRepository> repositories = gitDataService.getRepositoriesByPlatform(courseId, platform);
            return Result.success(repositories);
        } catch (Exception e) {
            return Result.error("查询Git仓库列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有启用的Git仓库列表（调试用）
     */
    @ApiOperation("获取所有启用的Git仓库列表")
    @GetMapping("/repositories/all")
    public Result<List<GitRepository>> getAllRepositories() {
        try {
            List<GitRepository> repositories = gitDataService.getActiveRepositories();
            System.out.println("[DEBUG] GitDataController.getAllRepositories - 查询到的启用仓库数量: " + (repositories != null ? repositories.size() : 0));
            if (repositories != null && !repositories.isEmpty()) {
                for (int i = 0; i < repositories.size(); i++) {
                    GitRepository repo = repositories.get(i);
                    System.out.println("[DEBUG] 启用仓库" + (i+1) + ": ID=" + repo.getRepositoryId() + ", Name=" + repo.getRepositoryName() + ", CourseId=" + repo.getCourseId() + ", IsActive=" + repo.getIsActive());
                }
            }
            return Result.success(repositories);
        } catch (Exception e) {
            System.out.println("[DEBUG] GitDataController.getAllRepositories - 发生异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("查询启用Git仓库失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据仓库URL查询Git仓库
     */
    @ApiOperation("根据仓库URL查询Git仓库")
    @GetMapping("/repositories/url")
    public Result<GitRepository> getRepositoryByUrl(@ApiParam("仓库URL") @RequestParam String repositoryUrl) {
        try {
            GitRepository repository = gitDataService.getRepositoryByUrl(repositoryUrl);
            if (repository != null) {
                return Result.success(repository);
            } else {
                return Result.error("Git仓库不存在");
            }
        } catch (Exception e) {
            return Result.error("查询Git仓库失败: " + e.getMessage());
        }
    }
    
    /**
     * 同步Git仓库数据
     */
    @ApiOperation("同步Git仓库数据")
    @PostMapping("/repositories/{repositoryId}/sync")
    public Result<Boolean> syncRepositoryData(@ApiParam("仓库ID") @PathVariable Integer repositoryId) {
        try {
            boolean success = gitDataService.syncRepositoryData(repositoryId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("Git仓库数据同步失败");
            }
        } catch (Exception e) {
            return Result.error("Git仓库数据同步失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加Git活动记录
     */
    @ApiOperation("添加Git活动记录")
    @PostMapping("/activities")
    public Result<Boolean> addGitActivity(@Valid @RequestBody GitActivity gitActivity) {
        try {
            boolean success = gitDataService.addGitActivity(gitActivity);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("Git活动记录添加失败");
            }
        } catch (Exception e) {
            return Result.error("Git活动记录添加失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量添加Git活动记录
     */
    @ApiOperation("批量添加Git活动记录")
    @PostMapping("/activities/batch")
    public Result<Boolean> batchAddGitActivities(@Valid @RequestBody List<GitActivity> activities) {
        try {
            boolean success = gitDataService.batchAddGitActivities(activities);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("Git活动记录批量添加失败");
            }
        } catch (Exception e) {
            return Result.error("Git活动记录批量添加失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据仓库ID分页查询Git活动记录
     */
    @ApiOperation("根据仓库ID分页查询Git活动记录")
    @GetMapping("/activities/repository/{repositoryId}")
    public Result<IPage<GitActivity>> getActivitiesByRepositoryId(
            @ApiParam("仓库ID") @PathVariable Integer repositoryId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            IPage<GitActivity> activities = gitDataService.getActivitiesByRepositoryId(repositoryId, pageNum, pageSize);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error("查询Git活动记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据成员ID查询Git活动记录
     */
    @ApiOperation("根据成员ID查询Git活动记录")
    @GetMapping("/activities/member/{memberId}")
    public Result<List<GitActivity>> getActivitiesByMemberId(
            @ApiParam("成员ID") @PathVariable Integer memberId,
            @ApiParam("开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            List<GitActivity> activities = gitDataService.getActivitiesByMemberId(memberId, startTime, endTime);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error("查询Git活动记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据活动类型查询Git活动记录
     */
    @ApiOperation("根据活动类型查询Git活动记录")
    @GetMapping("/activities/repository/{repositoryId}/type/{activityType}")
    public Result<List<GitActivity>> getActivitiesByType(
            @ApiParam("仓库ID") @PathVariable Integer repositoryId,
            @ApiParam("活动类型") @PathVariable String activityType,
            @ApiParam("开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            List<GitActivity> activities = gitDataService.getActivitiesByType(repositoryId, activityType, startTime, endTime);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error("查询Git活动记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取成员在指定时间段内的提交统计
     */
    @ApiOperation("获取成员在指定时间段内的提交统计")
    @GetMapping("/activities/member/{memberId}/repository/{repositoryId}/stats")
    public Result<GitActivity> getMemberCommitStats(
            @ApiParam("成员ID") @PathVariable Integer memberId,
            @ApiParam("仓库ID") @PathVariable Integer repositoryId,
            @ApiParam("开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            GitActivity stats = gitDataService.getMemberCommitStats(memberId, repositoryId, startTime, endTime);
            if (stats != null) {
                return Result.success(stats);
            } else {
                return Result.error("暂无统计数据");
            }
        } catch (Exception e) {
            return Result.error("查询提交统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除指定时间之前的Git活动记录
     */
    @ApiOperation("删除指定时间之前的Git活动记录")
    @DeleteMapping("/activities/before")
    public Result<Boolean> deleteActivitiesBefore(
            @ApiParam("时间点") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeTime) {
        try {
            boolean success = gitDataService.deleteActivitiesBefore(beforeTime);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("Git活动记录删除失败");
            }
        } catch (Exception e) {
            return Result.error("Git活动记录删除失败: " + e.getMessage());
        }
    }
}