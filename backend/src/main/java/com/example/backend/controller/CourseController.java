package com.example.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.common.Result;
import com.example.backend.entity.Course;
import com.example.backend.entity.CourseEnrollment;
import com.example.backend.service.CourseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 课程管理控制器
 */
@Api(tags = "课程管理")
@RestController
@RequestMapping("/courses")
@CrossOrigin
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    /**
     * 创建课程
     */
    @ApiOperation("创建课程")
    @PostMapping
    public Result<Boolean> createCourse(@Valid @RequestBody Course course) {
        try {
            boolean success = courseService.createCourse(course);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("课程创建失败");
            }
        } catch (Exception e) {
            return Result.error("课程创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新课程信息
     */
    @ApiOperation("更新课程信息")
    @PutMapping("/{courseId}")
    public Result<Boolean> updateCourse(
            @ApiParam("课程ID") @PathVariable Integer courseId,
            @Valid @RequestBody Course course) {
        try {
            course.setCourseId(courseId);
            course.setCourseDescription(course.getCourseDescription());
            boolean success = courseService.updateCourse(course);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("课程更新失败");
            }
        } catch (Exception e) {
            return Result.error("课程更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除课程
     */
    @ApiOperation("删除课程")
    @DeleteMapping("/{courseId}")
    public Result<Boolean> deleteCourse(@ApiParam("课程ID") @PathVariable Integer courseId) {
        try {
            boolean success = courseService.deleteCourse(courseId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("课程删除失败");
            }
        } catch (Exception e) {
            return Result.error("课程删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询课程
     */
    @ApiOperation("根据ID查询课程")
    @GetMapping("/{courseId}")
    public Result<Course> getCourseById(@ApiParam("课程ID") @PathVariable Integer courseId) {
        try {
            Course course = courseService.getCourseById(courseId);
            if (course != null) {
                return Result.success(course);
            } else {
                return Result.error("课程不存在");
            }
        } catch (Exception e) {
            return Result.error("查询课程失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据组织ID分页查询课程
     */
    @ApiOperation("根据组织ID分页查询课程")
    @GetMapping
    public Result<IPage<Course>> getCoursesByOrganizationId(
            @ApiParam("组织ID") @RequestParam Integer organizationId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            IPage<Course> courses = courseService.getCoursesByOrganizationId(organizationId, pageNum, pageSize);
            return Result.success(courses);
        } catch (Exception e) {
            return Result.error("查询课程列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据教师ID查询课程列表
     */
    @ApiOperation("根据教师ID查询课程列表")
    @GetMapping("/teacher/{teacherId}")
    public Result<List<Course>> getCoursesByTeacherId(@ApiParam("教师ID") @PathVariable Integer teacherId) {
        try {
            List<Course> courses = courseService.getCoursesByTeacherId(teacherId);
            return Result.success(courses);
        } catch (Exception e) {
            return Result.error("查询教师课程列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据状态查询课程列表
     */
    @ApiOperation("根据状态查询课程列表")
    @GetMapping("/status/{status}")
    public Result<List<Course>> getCoursesByStatus(
            @ApiParam("组织ID") @RequestParam Integer organizationId,
            @ApiParam("课程状态") @PathVariable String status) {
        try {
            List<Course> courses = courseService.getCoursesByStatus(organizationId, status);
            return Result.success(courses);
        } catch (Exception e) {
            return Result.error("查询课程列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 学生注册课程
     */
    @ApiOperation("学生注册课程")
    @PostMapping("/{courseId}/enroll")
    public Result<Boolean> enrollStudents(
            @ApiParam("课程ID") @PathVariable Integer courseId,
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> memberIds = (List<Integer>) request.get("memberIds");
            
            boolean success = courseService.enrollStudents(courseId, memberIds);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("学生注册失败");
            }
        } catch (Exception e) {
            return Result.error("学生注册失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取课程学生列表
     */
    @ApiOperation("获取课程学生列表")
    @GetMapping("/{courseId}/students")
    public Result<List<CourseEnrollment>> getCourseStudents(@ApiParam("课程ID") @PathVariable Integer courseId) {
        try {
            List<CourseEnrollment> students = courseService.getCourseStudents(courseId);
            return Result.success(students);
        } catch (Exception e) {
            return Result.error("查询课程学生列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 移除课程学生
     */
    @ApiOperation("移除课程学生")
    @DeleteMapping("/{courseId}/students/{memberId}")
    public Result<Boolean> removeStudent(
            @ApiParam("课程ID") @PathVariable Integer courseId,
            @ApiParam("成员ID") @PathVariable Integer memberId) {
        try {
            boolean success = courseService.removeStudent(courseId, memberId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("学生移除失败");
            }
        } catch (Exception e) {
            return Result.error("学生移除失败: " + e.getMessage());
        }
    }
    
}