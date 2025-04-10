package com.example.thedayoftoday.domain.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequestDto(
        @NotBlank(message = "이름은 필수 입력 값입니다.") String name,
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수 입력 값입니다.") String email,
        @NotBlank(message = "비밀번호는 필수 입력 값입니다.") String password,
        @NotBlank(message = "전화번호는 필수 입력 값입니다.") String phoneNumber
) {
}
