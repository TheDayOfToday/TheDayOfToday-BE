package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.LoginRequestDto;
import com.example.thedayoftoday.domain.dto.SignupRequestDto;
import com.example.thedayoftoday.domain.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/join")
    public String joinProcess(@RequestBody @Valid SignupRequestDto signupRequestDto) {

        authService.authProcess(signupRequestDto);
        return "ok";
    }

}
