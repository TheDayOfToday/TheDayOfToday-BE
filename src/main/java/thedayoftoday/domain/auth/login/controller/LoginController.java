package thedayoftoday.domain.auth.login.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import thedayoftoday.domain.auth.login.dto.LoginRequestDto;
import thedayoftoday.domain.auth.login.service.LoginService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/swagger-auth") // 경로 변경하장~
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDto loginDto) {
        String accessToken = loginService.login(loginDto);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);

        return ResponseEntity.ok(response);
    }
}
