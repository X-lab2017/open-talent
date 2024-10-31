package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("organizations")
public class Organization {
    @TableId(type = IdType.AUTO)
    private Integer organizationId;
    private String name;
}
