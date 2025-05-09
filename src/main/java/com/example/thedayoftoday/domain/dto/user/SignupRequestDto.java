package com.example.thedayoftoday.domain.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequestDto(
        @NotBlank(message = "이름은 필수 입력 값입니다.") String name,
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수 입력 값입니다.") String email,
        @NotBlank(message = "비밀번호는 필수 입력 값입니다.") String password,
        @Pattern(regexp = "(01[016789])(\\d{3,4})(\\d{4})", message = "올바른 휴대폰 번호를 입력해주세요.")
        @NotBlank(message = "전화번호는 필수 입력 값입니다.") String phoneNumber
) {
}
