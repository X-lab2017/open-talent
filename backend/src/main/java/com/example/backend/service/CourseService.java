package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.entity.Course;
import com.example.backend.entity.CourseEnrollment;
import com.example.backend.entity.Member;
import com.example.backend.mapper.CourseMapper;
import com.example.backend.mapper.CourseEnrollmentMapper;
import com.example.backend.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程管理服务类
 */
@Service
public class CourseService {
    
    @Autowired
    private CourseMapper courseMapper;
    
    @Autowired
    private CourseEnrollmentMapper courseEnrollmentMapper;
    
    @Autowired
    private MemberMapper memberMapper;
    
    /**
     * 创建课程
     * @param course 课程信息
     * @return 创建结果
     */
    @Transactional
    public boolean createCourse(Course course) {
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        if (course.getStatus() == null) {
            course.setStatus("active");
        }
        return courseMapper.insert(course) > 0;
    }
    
    /**
     * 更新课程信息
     * @param course 课程信息
     * @return 更新结果
     */
    @Transactional
    public boolean updateCourse(Course course) {
        course.setUpdatedAt(LocalDateTime.now());
        return courseMapper.updateById(course) > 0;
    }
    
    /**
     * 删除课程
     * @param courseId 课程ID
     * @return 删除结果
     */
    @Transactional
    public boolean deleteCourse(Integer courseId) {
        return courseMapper.deleteById(courseId) > 0;
    }
    
    /**
     * 根据ID查询课程
     * @param courseId 课程ID
     * @return 课程信息
     */
    public Course getCourseById(Integer courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course != null) {
            List<CourseEnrollment> enrollments = courseEnrollmentMapper.selectByCourseId(courseId);
            course.setStudentCount(enrollments != null ? enrollments.size() : 0);
        }
        return course;
    }
    
    /**
     * 根据组织ID分页查询课程
     * @param organizationId 组织ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 课程分页数据
     */
    public IPage<Course> getCoursesByOrganizationId(Integer organizationId, Integer pageNum, Integer pageSize) {

        Page<Course> page = new Page<>(pageNum, pageSize);
        IPage<Course> coursePage = courseMapper.selectByOrganizationId(page, organizationId);

        // 为每个课程设置学生数量
        List<Course> courses = coursePage.getRecords();
        for (Course course : courses) {

            List<CourseEnrollment> enrollments = courseEnrollmentMapper.selectByCourseId(course.getCourseId());
            int studentCount = enrollments != null ? enrollments.size() : 0;
            

            course.setStudentCount(studentCount);
            
        }

        return coursePage;
    }
    
    /**
     * 根据教师ID查询课程列表
     * @param teacherId 教师ID
     * @return 课程列表
     */
    public List<Course> getCoursesByTeacherId(Integer teacherId) {
        List<Course> courses = courseMapper.selectByTeacherId(teacherId);
        
        // 为每个课程设置学生数量
        for (Course course : courses) {
            List<CourseEnrollment> enrollments = courseEnrollmentMapper.selectByCourseId(course.getCourseId());
            course.setStudentCount(enrollments != null ? enrollments.size() : 0);
        }
        
        return courses;
    }
    
    /**
     * 根据状态查询课程列表
     * @param organizationId 组织ID
     * @param status 课程状态
     * @return 课程列表
     */
    public List<Course> getCoursesByStatus(Integer organizationId, String status) {
        List<Course> courses = courseMapper.selectByStatus(organizationId, status);
        
        // 为每个课程设置学生数量
        for (Course course : courses) {
            List<CourseEnrollment> enrollments = courseEnrollmentMapper.selectByCourseId(course.getCourseId());
            course.setStudentCount(enrollments != null ? enrollments.size() : 0);
        }
        
        return courses;
    }
    
    /**
     * 学生注册课程
     * @param courseId 课程ID
     * @param memberIds 成员ID列表
     * @return 注册结果
     */
    @Transactional
    public boolean enrollStudents(Integer courseId, List<Integer> memberIds) {
        try {
            for (Integer memberId : memberIds) {
                // 检查是否已经注册
                CourseEnrollment existing = courseEnrollmentMapper.selectByCourseIdAndMemberId(courseId, memberId);
                if (existing == null) {
                    // 获取成员信息
                    Member member = memberMapper.selectById(memberId);
                    if (member == null) {
                        throw new RuntimeException("成员不存在: " + memberId);
                    }
                    
                    CourseEnrollment enrollment = new CourseEnrollment();
                    enrollment.setCourseId(courseId);
                    enrollment.setMemberId(memberId);
                    enrollment.setMemberName(member.getName());
                    enrollment.setMemberEmail(member.getContactEmail());
                    enrollment.setEnrollmentDate(LocalDateTime.now());
                    enrollment.setCreatedAt(LocalDateTime.now());
                    enrollment.setUpdatedAt(LocalDateTime.now());
                    enrollment.setStatus("active");
                    courseEnrollmentMapper.insert(enrollment);
                }
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("学生注册课程失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取课程学生列表
     * @param courseId 课程ID
     * @return 注册记录列表
     */
    public List<CourseEnrollment> getCourseStudents(Integer courseId) {
        return courseEnrollmentMapper.selectByCourseId(courseId);
    }
    
    /**
     * 移除课程学生
     * @param courseId 课程ID
     * @param memberId 成员ID
     * @return 移除结果
     */
    @Transactional
    public boolean removeStudent(Integer courseId, Integer memberId) {
        QueryWrapper<CourseEnrollment> wrapper = new QueryWrapper<>();
        wrapper.eq("course_id", courseId).eq("member_id", memberId);
        return courseEnrollmentMapper.delete(wrapper) > 0;
    }
    

}