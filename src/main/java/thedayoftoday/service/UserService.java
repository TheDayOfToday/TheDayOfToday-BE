package thedayoftoday.service;

import thedayoftoday.dto.ResetPasswordRequestDto;
import thedayoftoday.dto.setting.UserInfoDto;
import thedayoftoday.dto.user.PasswordUpdateRequest;
import thedayoftoday.entity.User;
import thedayoftoday.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        updatePasswordInternal(user, request.newPassword());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        updatePasswordInternal(user, requestDto.newPassword());
    }

    private void updatePasswordInternal(User user, String newPassword) {
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("기존의 비밀번호와 동일합니다.");
        }
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 계정을 찾을 수 없습니다."));
        userRepository.delete(user);
    }
}