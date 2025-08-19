package thedayoftoday.domain.auth.service;

import thedayoftoday.domain.auth.dto.EmailCondeValidationDto;
import thedayoftoday.domain.auth.dto.SendCodeRequestDto;
import thedayoftoday.domain.auth.dto.SignupRequestDto;
import thedayoftoday.domain.user.entity.User;
import thedayoftoday.domain.user.entity.RoleType;
import thedayoftoday.domain.auth.exception.EmailCodeNotMatchException;
import thedayoftoday.domain.auth.exception.EmailDuplicationException;
import thedayoftoday.domain.auth.exception.PhoneNumberDuplicationExceptiono;
import thedayoftoday.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thedayoftoday.domain.user.service.UserService;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final MailSendService mailSendService;

    public void checkEmailExists(String email) {
        userService.checkEmailExists(email);
    }

    public void sendVerificationCode(SendCodeRequestDto sendCodeRequestDto) {
        mailSendService.sendCode(sendCodeRequestDto);
    }

    public void verifyCode(EmailCondeValidationDto emailCondeValidationDto) {
        String code = mailSendService.getCodeFromRedis(emailCondeValidationDto.email());
        if (!code.equals(emailCondeValidationDto.code())) {
            throw new EmailCodeNotMatchException("인증번호가 일치하지 않습니다.");
        }
    }

    public Long join(@Valid SignupRequestDto user) {
        return userService.createUser(user);
    }
}
