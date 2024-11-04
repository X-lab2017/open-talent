package com.example.backend.service;

import com.example.backend.entity.Member;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface MemberService {
    void singleRegister(Member req);

    void batchRegister(MultipartFile file) throws IOException;

    Map<Integer,String> getOrgName();
}
