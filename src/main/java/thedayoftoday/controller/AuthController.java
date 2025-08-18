package thedayoftoday.controller;

import thedayoftoday.dto.ResetPasswordRequestDto;
import thedayoftoday.dto.user.EmailCondeValidationDto;
import thedayoftoday.dto.user.SendCodeRequestDto;
import thedayoftoday.dto.user.SignupRequestDto;
import thedayoftoday.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user") //프론트 고치면 "/auth"로 고칠 예정
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        authService.join(signupRequestDto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @GetMapping("/find-email")
    public ResponseEntity<String> checkEmail(String email) {
        authService.checkEmailExists(email);
        return ResponseEntity.ok("사용 가능한 이메일입니다.");
    }

    @PostMapping("/send-code")
    public ResponseEntity<String> sendCode(@RequestBody SendCodeRequestDto sendCodeRequestDto) {
        authService.sendVerificationCode(sendCodeRequestDto);
        return ResponseEntity.ok("인증 코드가 발송되었습니다.");
    }

    @PostMapping("/check-code")
    public ResponseEntity<String> checkCode(@RequestBody EmailCondeValidationDto emailCondeValidationDto) {
        authService.verifyCode(emailCondeValidationDto);
        return ResponseEntity.ok("정상적으로 인증되었습니다.");
    }
}