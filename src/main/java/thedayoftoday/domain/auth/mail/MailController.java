package thedayoftoday.domain.auth.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user") // 프론트와 함께 경로 수정할 예정
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @PostMapping("/send-code")
    public ResponseEntity<String> sendCode(@RequestBody SendCodeRequestDto sendCodeRequestDto) {
        mailService.sendVerificationCode(sendCodeRequestDto);
        return ResponseEntity.ok("인증 코드가 발송되었습니다.");
    }

    @PostMapping("/check-code")
    public ResponseEntity<String> checkCode(@RequestBody EmailCondeValidationDto emailCondeValidationDto) {
        mailService.verifyCode(emailCondeValidationDto);
        return ResponseEntity.ok("정상적으로 인증되었습니다.");
    }
}
