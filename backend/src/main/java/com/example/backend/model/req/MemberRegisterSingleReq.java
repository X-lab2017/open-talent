package com.example.backend.model.req;

import lombok.Data;

@Data
public class MemberRegisterSingleReq {
    private Integer memberId;
    private String name;
    private String nationality;
    private Integer organizationId;
    private String githubAccount;
    private String giteeAccount;
    private String contactEmail;
    private Integer openrankValue;
    private String community;
    private Integer active_months;
}
