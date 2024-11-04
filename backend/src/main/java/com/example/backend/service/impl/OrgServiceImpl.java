package com.example.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.domain.Org;
import com.example.backend.entity.Member;
import com.example.backend.service.OrgService;
import com.example.backend.mapper.OrgMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}




