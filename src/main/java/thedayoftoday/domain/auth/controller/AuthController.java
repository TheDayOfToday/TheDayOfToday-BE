package thedayoftoday.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import thedayoftoday.domain.auth.dto.LoginRequestDto;
import thedayoftoday.domain.auth.dto.SignupRequestDto;
import thedayoftoday.domain.auth.service.AuthService;

import java.util.HashMap;
import java.util.Map;

@RestController
//@RequestMapping("/auth") //프론트 고치면 "/auth"로 고칠 예정
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/user/signup")
    public ResponseEntity<String> pastSignup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        authService.join(signupRequestDto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        authService.join(signupRequestDto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @PostMapping("/swagger-auth/login")
    public ResponseEntity<Map<String, String>> pastLogin(@RequestBody LoginRequestDto loginDto) {
        String accessToken = authService.login(loginDto);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDto loginDto) {
        String accessToken = authService.login(loginDto);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);

        return ResponseEntity.ok(response);
    }
}