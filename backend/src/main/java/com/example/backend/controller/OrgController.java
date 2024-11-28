package com.example.backend.controller;

import com.example.backend.common.JwtUtil;
import com.example.backend.common.Result;
import com.example.backend.entity.Org;
import com.example.backend.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/org")
public class OrgController {
    @Autowired
    OrgService orgServiceImpl;

    @PostMapping("/register")
    public Result<Void> orgRegister(@RequestBody Org org) {
        orgServiceImpl.orgRegister(org);
        return Result.success();
    }

    @GetMapping("/search")
    public Result<List<Org>> searchOrganizations() {  // 返回类型改为 List<Org>
        List<Org> organizations = orgServiceImpl.getAllOrganizations();  // 获取完整的 Org 对象列表
        return Result.success(organizations);
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Org org) {
        Org existingOrg = orgServiceImpl.findByName(org.getName());
        if (existingOrg != null && existingOrg.getPassword().equals(org.getPassword())) {
            String token = JwtUtil.generateToken(existingOrg.getName());
            Map<String, String> data = new HashMap<>();
            data.put("token", token);
            data.put("name", existingOrg.getName());
            return Result.success(data);
        }
        return Result.error("用户名或密码错误");
    }
}
