package com.example.backend.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Member;
import com.example.backend.mapper.MemberMapper;
import com.example.backend.model.excel.MemberModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.backend.service.MemberService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.example.backend.model.RetPage.EntityToVO;

@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Override
    public void singleRegister(Member req) {
        baseMapper.insert(req);
    }

    @Override
    public void batchRegister(MultipartFile file) throws IOException {
        List<MemberModel> memberModelList = EasyExcel.read(file.getInputStream()).head(MemberModel.class).sheet().doReadSync();
        List<Member> entities = EntityToVO(memberModelList, Member.class);
        entities.forEach(e -> {
            baseMapper.insert(e);
        });
    }
}
