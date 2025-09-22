package thedayoftoday.domain.auth.mail;

import thedayoftoday.domain.auth.mail.exception.EmailCodeExpireException;
import thedayoftoday.domain.auth.mail.exception.EmailCodeNotMatchException;
import thedayoftoday.domain.auth.mail.exception.MailSendException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MailControllerTest {

    @InjectMocks
    private MailController mailController;

    @Mock
    private MailService mailService;

    @RestControllerAdvice
    static class TestAdvice {
        @ExceptionHandler(MailSendException.class)
        public ResponseEntity<String> handleMailSend(MailSendException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }

        @ExceptionHandler(EmailCodeNotMatchException.class)
        public ResponseEntity<String> handleNotMatch(EmailCodeNotMatchException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        @ExceptionHandler(EmailCodeExpireException.class)
        public ResponseEntity<String> handleExpire(EmailCodeExpireException e) {
            return ResponseEntity.status(410).body(e.getMessage());
        }
    }

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(mailController)
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter()
                )
                .addFilters(new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true))
                .setControllerAdvice(new TestAdvice())
                .build();
    }

    // ===== /user/mail/send-code =====

    @Test
    @DisplayName("POST /user/mail/send-code -> 200 OK + 안내 메시지")
    void sendCode_success() throws Exception {
        String body = """
                {"email":"user@ex.com"}
                """;

        mvc().perform(post("/user/mail/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("인증 코드가 발송되었습니다."));

        verify(mailService).sendVerificationCode(argThat(d -> "user@ex.com".equals(d.email())));
    }

    @Test
    @DisplayName("POST /user/mail/send-code -> 메일 전송 실패 시 500 + 에러 메시지")
    void sendCode_fail_mailSend() throws Exception {
        String body = """
                {"email":"user@ex.com"}
                """;

        willThrow(new MailSendException("이메일 전송 중 오류가 발생했습니다."))
                .given(mailService)
                .sendVerificationCode(argThat(d -> "user@ex.com".equals(d.email())));

        mvc().perform(post("/user/mail/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("이메일 전송 중 오류가 발생했습니다."));
    }

    // ===== /user/mail/check-code =====

    @Test
    @DisplayName("POST /user/mail/check-code -> 200 OK + 안내 메시지")
    void checkCode_success() throws Exception {
        String body = """
                {"email":"user@ex.com","code":"ABC12345"}
                """;

        mvc().perform(post("/user/mail/check-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("정상적으로 인증되었습니다."));

        verify(mailService).verifyCode(argThat(d ->
                "user@ex.com".equals(d.email()) && "ABC12345".equals(d.code())
        ));
    }

    @Test
    @DisplayName("POST /user/mail/check-code -> 코드 불일치 시 400 + 메시지")
    void checkCode_notMatch() throws Exception {
        String body = """
                {"email":"user@ex.com","code":"WRONG"}
                """;

        willThrow(new EmailCodeNotMatchException("인증번호가 일치하지 않습니다."))
                .given(mailService)
                .verifyCode(argThat(d ->
                        "user@ex.com".equals(d.email()) && "WRONG".equals(d.code())
                ));

        mvc().perform(post("/user/mail/check-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("인증번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("POST /user/mail/check-code -> 코드 만료 시 410 + 메시지")
    void checkCode_expired() throws Exception {
        String body = """
                {"email":"user@ex.com","code":"ANY"}
                """;

        willThrow(new EmailCodeExpireException("인증번호가 만료되었습니다."))
                .given(mailService)
                .verifyCode(argThat(d ->
                        "user@ex.com".equals(d.email()) && "ANY".equals(d.code())
                ));

        mvc().perform(post("/user/mail/check-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isGone())
                .andExpect(content().string("인증번호가 만료되었습니다."));
    }
}
