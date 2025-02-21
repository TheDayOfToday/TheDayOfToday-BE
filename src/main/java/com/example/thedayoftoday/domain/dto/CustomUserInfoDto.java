package com.example.thedayoftoday.domain.dto;

import com.example.thedayoftoday.domain.entity.enumType.RoleType;
import lombok.Getter;

@Getter
public class CustomUserInfoDto {

    private Long userId;
    private String nickname;
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private RoleType role;
}
