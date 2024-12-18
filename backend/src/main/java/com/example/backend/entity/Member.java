package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("members")
public class Member {
    @TableId(type = IdType.AUTO)
    private Integer memberId;
    private String name;
    private String nationality;
    private Integer organizationId;
    private String githubAccount;
    private String giteeAccount;
    private String atomgitAccount;
    private String contactEmail;
    private String contactAddress;
    private Float openrankValue;
    private Float lastOpenrank;
    private String community;
    private Integer activeMonths;
}
