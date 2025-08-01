package com.deepanshu.devlog.dto;

import com.deepanshu.devlog.utils.MemberRole;

import lombok.Data;

@Data
public class AddMemberRequest {

    private String username;
    private MemberRole role;
}
