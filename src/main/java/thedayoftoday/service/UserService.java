package thedayoftoday.service;

import thedayoftoday.dto.user.SignupRequestDto;
import thedayoftoday.entity.User;
import thedayoftoday.entity.enumType.RoleType;
import thedayoftoday.exception.PhoneNumberDuplicationExceptiono;
import thedayoftoday.repository.UserRepository;
import thedayoftoday.exception.EmailDuplicationException;
import jakarta.validation.Valid;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public Long join(@Valid SignupRequestDto user) {
        boolean validUser = userRepository.existsByEmail(user.email());
        boolean validPhoneNumber = userRepository.existsByPhoneNumber(user.phoneNumber());

        validateDuplicate(validUser, validPhoneNumber);

        User newUser = User.builder()
                .name(user.name())
                .email(user.email())
                .password(encoder.encode(user.password()))
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

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
}
