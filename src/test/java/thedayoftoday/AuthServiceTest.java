package thedayoftoday;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import thedayoftoday.dto.user.SignupRequestDto;
import thedayoftoday.entity.User;
import thedayoftoday.repository.UserRepository;
import thedayoftoday.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class AuthServiceTest { // 클래스 이름 변경

    private final AuthService authService; // UserService -> AuthService
    private final UserRepository userRepository;

    @Autowired
    public AuthServiceTest(AuthService authService, UserRepository userRepository) { // 생성자 변경
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Test
    @DisplayName("회원가입 성공")
    void joinSuccess() {
        SignupRequestDto signupRequestDto = new SignupRequestDto(
                "Hun", "백광훈", "hun@aaaa.com", "010-1234-5678"
        );

        // 테스트 대상 메소드 변경!
        Long userId = authService.join(signupRequestDto); // userService.join -> authService.join

        User savedUser = userRepository.findById(userId).orElse(null);
        assertThat(savedUser.getEmail()).isEqualTo("hun@aaaa.com");
    }
}
