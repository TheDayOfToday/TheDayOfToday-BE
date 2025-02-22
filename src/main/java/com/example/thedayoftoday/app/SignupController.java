package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.SignupRequestDto;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.entity.enumType.RoleType;
import com.example.thedayoftoday.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

        Long id = userService.join(signupRequestDto);
        return ResponseEntity.ok(id);
    }
}
