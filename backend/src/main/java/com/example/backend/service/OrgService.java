package com.example.backend.service;

import com.example.backend.entity.Org;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author HDX
 * @description 针对表【organizations】的数据库操作Service
 * @createDate 2024-11-01 10:26:14
 */
public interface OrgService extends IService<Org> {
    void orgRegister(Org org);
    List<Org> getAllOrganizations();
    Org findByName(String name);
}
