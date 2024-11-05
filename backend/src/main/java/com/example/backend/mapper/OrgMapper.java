package com.example.backend.mapper;

import com.example.backend.domain.Org;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author HDX
 * @description 针对表【organizations】的数据库操作Mapper
 * @createDate 2024-11-01 10:26:14
 * @Entity com.example.backend.domain.Org
 */
@Mapper
public interface OrgMapper extends BaseMapper<Org> {

}




