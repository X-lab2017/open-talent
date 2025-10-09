package com.example.backend.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Member;
import com.example.backend.entity.Org;
import com.example.backend.entity.Course;
import com.example.backend.entity.CourseEnrollment;
import com.example.backend.mapper.MemberMapper;
import com.example.backend.mapper.OrgMapper;
import com.example.backend.mapper.CourseEnrollmentMapper;
import com.example.backend.mapper.CourseMapper;
import com.example.backend.model.excel.MemberModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.backend.service.MemberService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.backend.model.RetPage.EntityToVO;

@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    @Autowired
    private OrgMapper orgMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private CourseEnrollmentMapper courseEnrollmentMapper;
    @Autowired
    private CourseMapper courseMapper;

    @Override
    public List<Member> getAllMembers() {
        return memberMapper.selectList(null);  // 获取所有成员
    }

    @Override
    public void singleRegister(Member req) {
        baseMapper.insert(req);
    }

    @Override
    public void batchRegister(MultipartFile file) throws IOException {
        List<MemberModel> memberModelList = EasyExcel.read(file.getInputStream()).head(MemberModel.class).sheet().doReadSync();
        List<Member> entities = EntityToVO(memberModelList, Member.class);
        entities.forEach(e -> {
            // 如果表中某一行成员所属的组织不存在，则此条不插入，但是整体不会报错
            LambdaQueryWrapper<Org> lq = new LambdaQueryWrapper<>();
            lq.eq(Org::getOrganizationId, e.getOrganizationId());
            Long cnt = orgMapper.selectCount(lq);
            if (cnt > 0) {
                baseMapper.insert(e);
            }
        });
    }

    @Override
    public Map<Integer, String> getOrgName() {
        LambdaQueryWrapper<Org> lq = new LambdaQueryWrapper<>();
        List<Org> orgs = orgMapper.selectList(lq);
        Map<Integer, String> res = new HashMap<>();
        orgs.forEach(org -> {
            res.put(org.getOrganizationId(),org.getName());
        });
        return res;
    }

    @Override
    public void deleteMemberById(Integer memberId) {
        baseMapper.deleteById(memberId);
    }

    @Override
    public void updateMember(Integer memberId, Member member) {
        member.setMemberId(memberId);
        baseMapper.updateById(member);
    }

    /**
     * JWT验证过程调用
     */
    @Override
    public List<Member> getMembersByOrgName(String orgName) {
        LambdaQueryWrapper<Org> orgQuery = new LambdaQueryWrapper<>();
        orgQuery.eq(Org::getName, orgName);
        Org org = orgMapper.selectOne(orgQuery);
        if (org == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(Member::getOrganizationId, org.getOrganizationId());
        return memberMapper.selectList(memberQuery);
    }

    @Override
    public boolean isMemberInOrg(Integer memberId, String orgName) {
        LambdaQueryWrapper<Org> orgQuery = new LambdaQueryWrapper<>();
        orgQuery.eq(Org::getName, orgName);
        Org org = orgMapper.selectOne(orgQuery);
        if (org == null) {
            return false;
        }
        LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(Member::getMemberId, memberId)
                .eq(Member::getOrganizationId, org.getOrganizationId());
        return memberMapper.selectCount(memberQuery) > 0;
    }

    @Override
    public List<Member> getAvailableMembersForCourse(Integer courseId) {
        System.out.println("=== getAvailableMembersForCourse Debug Log ===");
        System.out.println("1. 输入的courseId: " + courseId);
        
        // 通过courseId获取课程信息
        Course course = courseMapper.selectById(courseId);
        System.out.println("2. 查询到的course对象: " + course);
        if (course != null) {
            System.out.println("   - courseId: " + course.getCourseId());
            System.out.println("   - courseName: " + course.getCourseName());
            System.out.println("   - organizationId: " + course.getOrganizationId());
        }
        
        if (course == null) {
            System.out.println("课程不存在，返回空列表");
            return new java.util.ArrayList<>();
        }
        
        // 获取课程所属组织的所有成员
        LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(Member::getOrganizationId, course.getOrganizationId());
        List<Member> orgMembers = memberMapper.selectList(memberQuery);
        System.out.println("3. 组织ID " + course.getOrganizationId() + " 下的成员数量: " + orgMembers.size());
        System.out.print("   成员ID列表: ");
        orgMembers.forEach(member -> System.out.print(member.getMemberId() + " "));
        System.out.println();
        
        // 获取已注册该课程的成员ID列表
        List<CourseEnrollment> enrollments = courseEnrollmentMapper.selectByCourseId(courseId);
        System.out.println("4. 课程注册记录数量: " + enrollments.size());
        List<Integer> enrolledMemberIds = enrollments.stream()
                .map(CourseEnrollment::getMemberId)
                .collect(java.util.stream.Collectors.toList());
        System.out.print("   已注册成员ID列表: ");
        enrolledMemberIds.forEach(id -> System.out.print(id + " "));
        System.out.println();
        
        // 过滤出该组织下未注册该课程的成员
        List<Member> availableMembers = orgMembers.stream()
                .filter(member -> !enrolledMemberIds.contains(member.getMemberId()))
                .collect(java.util.stream.Collectors.toList());
        
        System.out.println("5. 最终可用成员数量: " + availableMembers.size());
        System.out.print("   可用成员ID列表: ");
        availableMembers.forEach(member -> System.out.print(member.getMemberId() + " "));
        System.out.println();
        System.out.println("=== Debug Log End ===");
        
        return availableMembers;
    }
}
