package com.example.backend.service;

import com.example.backend.entity.Member;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MemberService {
    void singleRegister(Member req);

    void batchRegister(MultipartFile file) throws IOException;

    void deleteMemberById(Integer memberId);

    void updateMember(Integer memberId, Member member);

    Map<Integer,String> getOrgName();

    List<Member> getAllMembers();

    /**
     * JWT验证过程调用
     */
    List<Member> getMembersByOrgName(String orgName);

    boolean isMemberInOrg(Integer memberId, String orgName);

    /**
     * 获取可以添加到指定课程的成员列表（排除已注册该课程的成员）
     * @param courseId 课程ID
     * @return 可添加的成员列表
     */
    List<Member> getAvailableMembersForCourse(Integer courseId);
}
