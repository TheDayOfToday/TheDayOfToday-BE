package thedayoftoday.service;

import thedayoftoday.dto.user.EmailCondeValidationDto;
import thedayoftoday.dto.user.SendCodeRequestDto;
import thedayoftoday.dto.user.SignupRequestDto;
import thedayoftoday.entity.User;
import thedayoftoday.entity.enumType.RoleType;
import thedayoftoday.exception.EmailCodeNotMatchException;
import thedayoftoday.exception.EmailDuplicationException;
import thedayoftoday.exception.PhoneNumberDuplicationExceptiono;
import thedayoftoday.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSendService mailSendService;

    public void checkEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailDuplicationException("이미 사용 중인 이메일입니다.");
        }
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
        boolean validUser = userRepository.existsByEmail(user.email());
        boolean validPhoneNumber = userRepository.existsByPhoneNumber(user.phoneNumber());

        validateDuplicate(validUser, validPhoneNumber);

        User newUser = User.builder()
                .name(user.name())
                .email(user.email())
                .password(passwordEncoder.encode(user.password()))
                .phoneNumber(user.phoneNumber())
                .role(RoleType.USER)
                .build();

        User savedUser = userRepository.save(newUser);
        return savedUser.getUserId();
    }

    private static void validateDuplicate(boolean validUser, boolean validPhoneNumber) {
        if (validUser) {
            throw new EmailDuplicationException("해당 Email은 이미 존재합니다.");
        }
        if (validPhoneNumber) {
            throw new PhoneNumberDuplicationExceptiono("해당 전화번호는 이미 존재합니다.");
        }
    }
}
