package thedayoftoday.domain.auth.service;

import thedayoftoday.domain.auth.dto.LoginRequestDto;
import thedayoftoday.domain.auth.dto.SignupRequestDto;
import thedayoftoday.domain.auth.security.CustomUserDetails;
import thedayoftoday.domain.auth.security.util.JWTUtil;
import thedayoftoday.domain.user.entity.RoleType;
import thedayoftoday.domain.user.service.UserService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks private AuthService authService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JWTUtil jwtUtil;
    @Mock private UserService userService;

    // -------------------- join --------------------
    @Test
    @DisplayName("join: UserService.createUser를 호출하고 생성된 userId를 반환")
    void join_callsUserService_andReturnsId() {
        // given
        SignupRequestDto req = new SignupRequestDto(
                "홍길동", "new@ex.com", "pw!", "010-1111-2222"
        );
        given(userService.createUser(req)).willReturn(123L);

        // when
        Long userId = authService.join(req);

        // then
        assertThat(userId).isEqualTo(123L);
        then(userService).should().createUser(req);
        verifyNoInteractions(authenticationManager, jwtUtil);
    }

    // -------------------- login (성공) --------------------
    @Test
    @DisplayName("login: 인증 성공 시 JWTUtil로 access 토큰을 생성해 반환")
    void login_success_returnsAccessToken() {
        // given
        LoginRequestDto req = new LoginRequestDto("user@ex.com", "secret");

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails principal = mock(CustomUserDetails.class);

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(principal);

        given(principal.getUsername()).willReturn("user@ex.com");
        given(principal.getRole()).willReturn(String.valueOf(RoleType.USER));
        given(principal.getUserId()).willReturn(10L);

        given(jwtUtil.createAccessToken("access", "user@ex.com", String.valueOf(RoleType.USER), 10L))
                .willReturn("fake.jwt.token");

        // when
        String token = authService.login(req);

        // then
        assertThat(token).isEqualTo("fake.jwt.token");

        // AuthenticationManager 호출 시 전달된 토큰(principal/credentials) 검증
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        then(authenticationManager).should().authenticate(captor.capture());
        UsernamePasswordAuthenticationToken sent = captor.getValue();
        assertThat(sent.getPrincipal()).isEqualTo("user@ex.com");
        assertThat(sent.getCredentials()).isEqualTo("secret");

        // JWT 발급 호출 검증
        then(jwtUtil).should().createAccessToken("access", "user@ex.com", String.valueOf(RoleType.USER), 10L);
    }

    // -------------------- login (실패) --------------------
    @Test
    @DisplayName("login: 자격 증명 오류면 BadCredentialsException을 전파")
    void login_badCredentials_propagates() {
        // given
        LoginRequestDto req = new LoginRequestDto("user@ex.com", "wrong");
        given(authenticationManager.authenticate(any(Authentication.class)))
                .willThrow(new BadCredentialsException("bad"));

        // when & then
        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);

        then(authenticationManager).should().authenticate(any(Authentication.class));
        verifyNoInteractions(jwtUtil);
    }
}
