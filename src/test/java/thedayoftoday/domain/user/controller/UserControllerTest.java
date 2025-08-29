package thedayoftoday.domain.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import thedayoftoday.domain.auth.mail.exception.EmailDuplicationException;
import thedayoftoday.domain.auth.security.CustomUserDetails;
import thedayoftoday.domain.user.dto.PasswordUpdateRequest;
import thedayoftoday.domain.user.dto.ResetPasswordRequestDto;
import thedayoftoday.domain.user.dto.UserInfoDto;
import thedayoftoday.domain.user.service.UserService;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @RestControllerAdvice
    static class TestAdvice {
        @ExceptionHandler(EmailDuplicationException.class)
        public ResponseEntity<String> handleEmailDup(EmailDuplicationException e) {
            return ResponseEntity.status(409).body("이미 존재");
        }
    }

    private MockMvc baseMvc(HandlerMethodArgumentResolver... resolvers) {
        return MockMvcBuilders.standaloneSetup(userController)
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter()
                )
                .addFilters(new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true))
                .setControllerAdvice(new TestAdvice())
                .setCustomArgumentResolvers(resolvers)
                .build();
    }


    private MockMvc mockMvc() {
        return baseMvc();
    }

    private MockMvc mockMvcWith(CustomUserDetails userDetails) {
        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(CustomUserDetails.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return userDetails;
            }
        };
        return baseMvc(authPrincipalResolver);
    }

    // ============== Tests ==============

    @Test
    @DisplayName("GET /user/info")
    void getSetting_success() throws Exception {
        // given
        long userId = 10L;
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        given(userDetails.getUserId()).willReturn(userId);

        UserInfoDto dto = new UserInfoDto(userId, "홍길동", "test@ex.com", "010-0000-0000");
        given(userService.getUserInfo(userId)).willReturn(dto);

        // when & then
        mockMvcWith(userDetails)
                .perform(get("/user/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("test@ex.com"))
                .andExpect(jsonPath("$.phoneNumber").value("010-0000-0000"));

        verify(userService).getUserInfo(userId);
    }

    @Test
    @DisplayName("GET /user/find-email")
    void findEmail_success() throws Exception {
        // given
        String email = "none@ex.com";

        // when & then
        mockMvc()
                .perform(get("/user/find-email").param("email", email))
                .andExpect(status().isOk())
                .andExpect(content().string("이메일이 존재합니다."));

        verify(userService).checkEmailExists(email);
    }

    @Test
    @DisplayName("GET /user/find-email -> 409 Conflict (EmailDuplicationException 매핑)")
    void findEmail_duplicate_conflict() throws Exception {
        // given
        String email = "dup@ex.com";
        doThrow(new EmailDuplicationException("이미 존재"))
                .when(userService).checkEmailExists(email);

        // when & then
        mockMvc()
                .perform(get("/user/find-email").param("email", email))
                .andExpect(status().isConflict())
                .andExpect(content().string("이미 존재"));

        verify(userService).checkEmailExists(email);
    }

    @Test
    @DisplayName("PUT /user/reset-password -> 200 OK + 메시지")
    void resetPassword_success() throws Exception {
        // given
        String body = """
                {"email":"x@y.com","newPassword":"newRaw"}
                """;

        // when & then
        mockMvc()
                .perform(put("/user/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("비밀번호 변경이 완료되었습니다."));

        verify(userService).resetPassword(argThat((ResetPasswordRequestDto r) ->
                "x@y.com".equals(r.email()) && "newRaw".equals(r.newPassword())
        ));
    }

    @Test
    @DisplayName("PUT /user/update-password -> 200 OK + 메시지")
    void updatePassword_success() throws Exception {
        // given
        long userId = 20L;
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        given(userDetails.getUserId()).willReturn(userId);

        String body = """
                {"newPassword":"npw"}
                """;

        // when & then
        mockMvcWith(userDetails)
                .perform(put("/user/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("비밀번호 변경이 완료되었습니다."));

        verify(userService).updatePassword(eq(userId), argThat((PasswordUpdateRequest r) ->
                "npw".equals(r.newPassword())
        ));
    }

    @Test
    @DisplayName("DELETE /user/delete -> 200 OK + 메시지")
    void deleteUser_success() throws Exception {
        // given
        long userId = 33L;
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        given(userDetails.getUserId()).willReturn(userId);

        // when & then
        mockMvcWith(userDetails)
                .perform(delete("/user/delete"))
                .andExpect(status().isOk())
                .andExpect(content().string("회원 삭제가 완료되었습니다."));

        verify(userService).deleteUser(userId);
    }
}
