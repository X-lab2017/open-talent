package com.example.backend.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class MemberModel {
    @ExcelProperty("姓名")
    private String name;
    @ExcelProperty("国家")
    private String nationality;
    @ExcelProperty("组织代码")
    private Integer organizationId;
    @ExcelProperty("GitHub账号")
    private String githubAccount;
    @ExcelProperty("Gitee账号")
    private String giteeAccount;
    @ExcelProperty("AtomGit账号")
    private String atomgitAccount;
    @ExcelProperty("联系邮箱")
    private String contactEmail;
    @ExcelProperty("通讯地址")
    private String contactAddress;
}
