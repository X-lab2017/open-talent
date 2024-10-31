package com.example.backend.controller;

import com.alibaba.excel.util.IoUtils;
import com.example.backend.entity.Member;
import com.example.backend.model.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.backend.service.MemberService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    public void registerDownliad(HttpServletResponse response) throws IOException {
        String filePath = "/template.xlsx";
        try (InputStream inputStream = this.getClass().getResourceAsStream(filePath);
            OutputStream outputStream = response.getOutputStream()) {
            IoUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
