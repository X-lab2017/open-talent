package com.example.backend.controller;

import com.alibaba.excel.util.IoUtils;
import com.example.backend.common.JwtUtil;
import com.example.backend.common.Result;
import com.example.backend.entity.Member;
import com.example.backend.model.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import com.example.backend.service.MemberService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberController {
    @Autowired
    private MemberService memberServiceImpl;

    @PostMapping("/register/single")
    public BaseResponse registerSingle(@RequestBody Member req){
        memberServiceImpl.singleRegister(req);
        return BaseResponse.success();
    }

    @PostMapping("/register/upload")
    public BaseResponse registerUpload(@RequestPart("file") MultipartFile file) throws IOException {
        memberServiceImpl.batchRegister(file);
        return BaseResponse.success();
    }

    @GetMapping("/register/download")
    public void registerDownload(HttpServletResponse response) throws IOException {
        String filePath = "/template.xlsx";
        try (InputStream inputStream = this.getClass().getResourceAsStream(filePath);
            OutputStream outputStream = response.getOutputStream()) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filePath + "\"");
            // 内容类型为通用类型，表示二进制数据流
            response.setContentType("application/octet-stream");
            IoUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/getOrgName")
    public BaseResponse getOrgName(){
        return BaseResponse.success(memberServiceImpl.getOrgName());
    }
    /**
     * 无JWT验证版本
     */
    @GetMapping("/search")
    public Result<List<Member>> searchMembers() {  // 新增的搜索接口
        List<Member> members = memberServiceImpl.getAllMembers();
        return Result.success(members);
    }

    @DeleteMapping("/delete/{memberId}")
    public Result deleteMember(@PathVariable Integer memberId) {
        memberServiceImpl.deleteMemberById(memberId);
        return Result.success();
    }

    @PutMapping("/edit/{memberId}")
    public Result editMember(@PathVariable Integer memberId, @RequestBody Member member) {
        memberServiceImpl.updateMember(memberId, member);
        return Result.success();
    }
    /**
     * JWT验证（~~~备用待测试，请勿删除！！！~~~）
     */
//    @GetMapping("/search")
//    public Result<List<Member>> searchMembers(@RequestHeader("Authorization") String token) {
//        String orgName = JwtUtil.extractUsername(token.replace("Bearer ", ""));
//        List<Member> members;
//        if ("admin".equals(orgName)) {
//            members = memberServiceImpl.getAllMembers();
//        } else {
//            members = memberServiceImpl.getMembersByOrgName(orgName);
//        }
//        return Result.success(members);
//    }
//
//    @DeleteMapping("/delete/{memberId}")
//    public Result deleteMember(@RequestHeader("Authorization") String token, @PathVariable Integer memberId) {
//        String orgName = JwtUtil.extractUsername(token.replace("Bearer ", ""));
//        if (!"admin".equals(orgName) && !memberServiceImpl.isMemberInOrg(memberId, orgName)) {
//            return Result.error("无权限删除该成员");
//        }
//        memberServiceImpl.deleteMemberById(memberId);
//        return Result.success();
//    }
//
//    @PutMapping("/edit/{memberId}")
//    public Result editMember(@RequestHeader("Authorization") String token, @PathVariable Integer memberId, @RequestBody Member member) {
//        String orgName = JwtUtil.extractUsername(token.replace("Bearer ", ""));
//        if (!"admin".equals(orgName) && !memberServiceImpl.isMemberInOrg(memberId, orgName)) {
//            return Result.error("无权限编辑该成员");
//        }
//        memberServiceImpl.updateMember(memberId, member);
//        return Result.success();
//    }
}
