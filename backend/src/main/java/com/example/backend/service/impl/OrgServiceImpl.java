package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Org;
import com.example.backend.service.OrgService;
import com.example.backend.mapper.OrgMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HDX
 * @description 针对表【organizations】的数据库操作Service实现
 * @createDate 2024-11-01 10:26:14
 */
@Service
public class OrgServiceImpl extends ServiceImpl<OrgMapper, Org>
        implements OrgService{
    @Autowired
    OrgMapper orgMapper;

    @Override
    public void orgRegister(Org org) {
        orgMapper.insert(org);
    }

    @Override
    public List<Org> getAllOrganizations() {
        return orgMapper.selectList(null);  // 返回完整的 Org 对象列表
    }
}




