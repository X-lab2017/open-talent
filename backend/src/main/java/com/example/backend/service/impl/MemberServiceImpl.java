package com.example.backend.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.Member;
import com.example.backend.entity.Org;
import com.example.backend.mapper.MemberMapper;
import com.example.backend.mapper.OrgMapper;
import com.example.backend.model.excel.MemberModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.backend.service.MemberService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.backend.model.RetPage.EntityToVO;

@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    @Autowired
    private OrgMapper orgMapper;
    @Autowired
    private MemberMapper memberMapper;

    @Override
    public List<Member> getAllMembers() {
        return memberMapper.selectList(null);  // 获取所有成员
    }

    @Override
    public void singleRegister(Member req) {
        baseMapper.insert(req);
    }

    @Override
    public void batchRegister(MultipartFile file) throws IOException {
        List<MemberModel> memberModelList = EasyExcel.read(file.getInputStream()).head(MemberModel.class).sheet().doReadSync();
        List<Member> entities = EntityToVO(memberModelList, Member.class);
        entities.forEach(e -> {
            // 如果表中某一行成员所属的组织不存在，则此条不插入，但是整体不会报错
            LambdaQueryWrapper<Org> lq = new LambdaQueryWrapper<>();
            lq.eq(Org::getOrganizationId, e.getOrganizationId());
            Long cnt = orgMapper.selectCount(lq);
            if (cnt > 0) {
                baseMapper.insert(e);
            }
        });
    }

    @Override
    public Map<Integer, String> getOrgName() {
        LambdaQueryWrapper<Org> lq = new LambdaQueryWrapper<>();
        List<Org> orgs = orgMapper.selectList(lq);
        Map<Integer, String> res = new HashMap<>();
        orgs.forEach(org -> {
            res.put(org.getOrganizationId(),org.getName());
        });
        return res;
    }
}
