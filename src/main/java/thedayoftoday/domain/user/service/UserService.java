package thedayoftoday.domain.user.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thedayoftoday.domain.auth.dto.SignupRequestDto;
import thedayoftoday.domain.auth.mail.exception.EmailDuplicationException;
import thedayoftoday.domain.auth.exception.PhoneNumberDuplicationExceptiono;
import thedayoftoday.domain.user.repository.UserRepository;
import thedayoftoday.domain.user.dto.PasswordUpdateRequest;
import thedayoftoday.domain.user.dto.ResetPasswordRequestDto;
import thedayoftoday.domain.user.dto.UserInfoDto;
import thedayoftoday.domain.user.entity.User;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    public UserInfoDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return new UserInfoDto(user.getUserId(), user.getName(), user.getEmail(), user.getPhoneNumber());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));
        user.updatePassword(requestDto.newPassword(), passwordEncoder);
    }

    @Transactional
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        user.updatePassword(request.newPassword(), passwordEncoder);
    }

    @Transactional
    public Long createUser(SignupRequestDto requestDto) {
        checkEmailExists(requestDto.email());
        checkPhoneNumberExists(requestDto.phoneNumber());

        User newUser = User.createUser(requestDto, passwordEncoder);
        return userRepository.save(newUser).getUserId();
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 계정을 찾을 수 없습니다."));
        userRepository.delete(user);
    }

    public void checkEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailDuplicationException("이미 사용 중인 이메일입니다.");
        }
    }

    private void checkPhoneNumberExists(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new PhoneNumberDuplicationExceptiono("해당 전화번호는 이미 존재합니다.");
        }
    }
}