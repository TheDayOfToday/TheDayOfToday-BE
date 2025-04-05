package com.example.thedayoftoday.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.example.thedayoftoday.domain.dto.user.SignupRequestDto;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.repository.UserRepository;
import com.example.thedayoftoday.domain.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class UserServiceTest {

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserServiceTest(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Test
    @DisplayName("회원가입 성공")
    void joinSuccess() {
        SignupRequestDto signupRequestDto = new SignupRequestDto(
                "Hun", "백광훈", "hun@aaaa.com", "010-1234-5678"
        );

        Long userId = userService.join(signupRequestDto);
        User savedUser = userRepository.findById(userId).orElse(null);
        assertThat(savedUser.getEmail()).isEqualTo("hun@aaaa.com");
    }

}
