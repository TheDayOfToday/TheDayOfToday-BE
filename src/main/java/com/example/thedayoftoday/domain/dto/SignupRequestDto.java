package com.example.thedayoftoday.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SignupRequestDto {
    private String nickname;
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
}
