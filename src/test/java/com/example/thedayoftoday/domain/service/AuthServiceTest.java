package com.example.thedayoftoday.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.thedayoftoday.domain.dto.LoginRequestDto;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.repository.UserRepository;
import com.example.thedayoftoday.domain.security.service.AuthService;
import com.example.thedayoftoday.domain.security.util.JwtUtil;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("로그인 성공 테스트")
    void login_success() {
        // Given
        String email = "test@example.com";
        String password = "1234";
        String encodedPassword = "encoded1234";
        String mockToken = "mockJwtToken";

        // Mock User 객체 생성
        User mockUser = User.builder()
                .email(email)
                .password(encodedPassword)
                .build();

        System.out.println("➡ [DEBUG] Mock 유저 생성: " + mockUser);

        doReturn(Optional.of(mockUser)).when(userRepository).findByEmail(email);
        System.out.println("➡ [DEBUG] findByEmail() 설정 완료");

        // 비밀번호 검증 설정
        doReturn(true).when(encoder).matches(password, encodedPassword);

        // JWT 토큰 생성 Mock 설정
        doReturn(mockToken).when(jwtUtil).createAccessToken(any());

        LoginRequestDto requestDto = new LoginRequestDto(email, password);

        // When
        String token = null;
        try {
            token = authService.login(requestDto);
        } catch (Exception e) {
            e.printStackTrace();
            fail("예외 발생: " + e.getMessage());
        }

        assertNotNull(token, "토큰이 null이 아님을 확인");
        assertEquals(mockToken, token, "JWT 토큰이 예상한 값과 일치하는지 확인");
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 이메일 없음")
    void login_fail_userNotFound() {
        // Given
        String email = "wrong@example.com";
        String password = "1234";

        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        LoginRequestDto requestDto = new LoginRequestDto(email, password);

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.login(requestDto);
        });

        assertEquals("해당 이메일이 존재하지 않습니다", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
        verify(encoder, never()).matches(anyString(), anyString());  //비밀번호 검증 호출 안됨(이메일이 없으니까)
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 비밀번호 틀림")
    void login_fail_wrongPassword() {
        // Given
        String email = "test@example.com";
        String password = "wrongPassword";
        String encodedPassword = "encoded1234";

        User mockUser = User.builder()
                .email(email)
                .password(encodedPassword)
                .build();

        doReturn(Optional.of(mockUser)).when(userRepository).findByEmail(email);
        doReturn(false).when(encoder).matches(password, encodedPassword);

        LoginRequestDto requestDto = new LoginRequestDto(email, password);

        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.login(requestDto);
        });

        assertEquals("비밀번호가 일치하지 않습니다.", exception.getMessage());

        verify(jwtUtil, never()).createAccessToken(any());  // JWT 토큰 생성 호출되선 안됨(비밀번호 일치 안하니까)
    }
}