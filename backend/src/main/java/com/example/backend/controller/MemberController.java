package com.example.backend.controller;

import com.alibaba.excel.util.IoUtils;
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

}
