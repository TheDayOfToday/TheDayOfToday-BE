package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.SignupRequestDto;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.entity.enumType.RoleType;
import com.example.thedayoftoday.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SignupController {

    private final UserService userService;


    @PostMapping("/signup")
    public ResponseEntity<Long> addUser(@Valid @RequestBody SignupRequestDto signupRequestDto) {

        User user = User.builder()
                .nickname(signupRequestDto.getNickname())
                .name(signupRequestDto.getName())
                .email(signupRequestDto.getEmail())
                .password(signupRequestDto.getPassword())  // 🚨 비밀번호를 명확히 설정
                .phoneNumber(signupRequestDto.getPhoneNumber())
                .role(RoleType.USER)
                .build();

        Long id = userService.join(user);
        return ResponseEntity.ok(id);
    }

}
