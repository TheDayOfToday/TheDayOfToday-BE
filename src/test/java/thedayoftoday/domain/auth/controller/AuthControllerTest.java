package thedayoftoday.domain.auth.controller;

import thedayoftoday.domain.auth.dto.SignupRequestDto;
import thedayoftoday.domain.auth.service.AuthService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    private MockMvc mockMvc() {
        return MockMvcBuilders.standaloneSetup(authController)
                // String 우선 등록하여 String 응답이 JSON 따옴표로 감싸지지 않게 함
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter()
                )
                .addFilters(new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true))
                .build();
    }

    // -------------------- 성공 케이스 --------------------

    @Test
    @DisplayName("POST /user/signup")
    void pastSignup_success() throws Exception {
        // DTO 정규식이 하이픈을 허용하지 않으므로 숫자만 전달
        String body = """
                {
                  "name":"홍길동",
                  "email":"ok@ex.com",
                  "password":"pass123!",
                  "phoneNumber":"01011112222"
                }
                """;

        given(authService.join(any(SignupRequestDto.class))).willReturn(1L);

        mockMvc()
                .perform(post("/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_PLAIN)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("회원가입이 완료되었습니다."));

        verify(authService).join(argThat(req ->
                "홍길동".equals(req.name())
                        && "ok@ex.com".equals(req.email())
                        && "pass123!".equals(req.password())
                        && "01011112222".equals(req.phoneNumber())
        ));
    }

    @Test
    @DisplayName("POST /auth/signup")
    void signup_success() throws Exception {
        String body = """
                {
                  "name":"김철수",
                  "email":"good@ex.com",
                  "password":"Pw0rd!!",
                  "phoneNumber":"01022223333"
                }
                """;

        given(authService.join(any(SignupRequestDto.class))).willReturn(2L);

        mockMvc()
                .perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_PLAIN)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("회원가입이 완료되었습니다."));

        verify(authService).join(argThat(req ->
                "김철수".equals(req.name())
                        && "good@ex.com".equals(req.email())
                        && "Pw0rd!!".equals(req.password())
                        && "01022223333".equals(req.phoneNumber())
        ));
    }

    // -------------------- 유효성 실패(참고) --------------------

    @Test
    @DisplayName("POST /user/signup")
    void pastSignup_validation_blank() throws Exception {
        String body = """
                {
                  "name":"",
                  "email":"",
                  "password":"",
                  "phoneNumber":""
                }
                """;

        mockMvc()
                .perform(post("/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /user/signup")
    void pastSignup_validation_invalidPattern() throws Exception {
        String body = """
                {
                  "name":"홍길동",
                  "email":"not-an-email",
                  "password":"pass123!",
                  "phoneNumber":"010-1111-2222"
                }
                """;

        mockMvc()
                .perform(post("/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/signup")
    void signup_validation_invalidPattern() throws Exception {
        String body = """
                {
                  "name":"김철수",
                  "email":"badEmail",
                  "password":"pw!",
                  "phoneNumber":"010-3333-4444"
                }
                """;

        mockMvc()
                .perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
