package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.entity.Org;
import com.example.backend.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/org")
public class OrgController {
    @Autowired
    OrgService orgServiceImpl;

    @PostMapping("/register")
    public Result orgRegister(@RequestBody Org org) {
        orgServiceImpl.orgRegister(org);
        return Result.success();
    }

}
