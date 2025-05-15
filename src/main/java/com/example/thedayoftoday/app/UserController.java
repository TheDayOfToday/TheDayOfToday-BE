package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.ResetPasswordRequestDto;
import com.example.thedayoftoday.domain.dto.user.EmailCondeValidationDto;
import com.example.thedayoftoday.domain.dto.user.PasswordUpdateRequest;
import com.example.thedayoftoday.domain.dto.user.SendCodeRequestDto;
import com.example.thedayoftoday.domain.dto.user.SignupRequestDto;
import com.example.thedayoftoday.domain.dto.setting.UserInfoDto;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.exception.EmailCodeNotMatchException;
import com.example.thedayoftoday.domain.repository.UserRepository;
import com.example.thedayoftoday.domain.security.CustomUserDetails;
import com.example.thedayoftoday.domain.service.MailSendService;
import com.example.thedayoftoday.domain.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final MailSendService mailSendService;

    public UserController(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder,
                          MailSendService mailSendService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.mailSendService = mailSendService;
    }

    @GetMapping("/info")
    public ResponseEntity<UserInfoDto> getSetting(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
        UserInfoDto userInfoDto = new UserInfoDto(userId, user.getName(), user.getEmail(), user.getPhoneNumber());
        return ResponseEntity.ok(userInfoDto);
    }

    @GetMapping("/find-email")
    public ResponseEntity<String> findEmail(String email) {
        if(!userRepository.existsByEmail(email)){
            throw new IllegalArgumentException("존재하지 않는 이메일입니다.");
        }
        return ResponseEntity.ok("이메일이 존재합니다.");
    }

    @PutMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequestDto restPasswordRequestDto) {

        User user = userRepository.findByEmail(restPasswordRequestDto.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (passwordEncoder.matches(restPasswordRequestDto.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("기존의 비밀번호와 동일합니다.");
        }

        String encodedPassword = passwordEncoder.encode(restPasswordRequestDto.newPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        return ResponseEntity.ok("비밀변호 변경이 완료되었습니다.");
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
        Long userId = userDetails.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않은 사용자입니다."));

        String newPassword = passwordUpdateRequest.newPassword();

        if (passwordEncoder.matches(passwordUpdateRequest.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("기존의 비밀번호와 동일합니다.");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        return ResponseEntity.ok("비밀변호 변경이 완료되었습니다.");
    }

    @PostMapping("/signup")
    public ResponseEntity<String> addUser(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        userService.join(signupRequestDto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @PostMapping(value = "/send-code")
    public ResponseEntity<String> sendCodeWithEmail(@RequestBody SendCodeRequestDto sendCodeRequestDto) {
        return ResponseEntity.ok(mailSendService.sendCode(sendCodeRequestDto));
    }

    @PostMapping("/check-code")
    public ResponseEntity<String> checkCode(@RequestBody EmailCondeValidationDto emailCondeValidationDto) {
        String code = mailSendService.getCodeFromRedis(emailCondeValidationDto.email());
        if (!code.equals(emailCondeValidationDto.code())) {
            throw new EmailCodeNotMatchException("두 인증번호가 다릅니다");
        }
        return ResponseEntity.ok("정상적으로 인증되었습니다.");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("해당 계정을 찾을 수 없습니다.");
        }
        userRepository.delete(user);
        return ResponseEntity.ok("회원 삭제가 완료되었습니다.");
    }
}
