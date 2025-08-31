package thedayoftoday.domain.auth.mail;

import thedayoftoday.domain.auth.mail.exception.EmailCodeExpireException;
import thedayoftoday.domain.auth.mail.exception.EmailCodeNotMatchException;
import thedayoftoday.domain.auth.mail.exception.MailSendException;

import jakarta.mail.Address;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @InjectMocks private MailService mailService;

    @Mock private JavaMailSender javaMailSender;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private TemplateEngine templateEngine;
    @Mock private ValueOperations<String, Object> valueOps;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(mailService, "senderEmail", "noreply@ex.com");
    }

    private void stubMailBuilding() {
        JavaMailSenderImpl realSender = new JavaMailSenderImpl();
        MimeMessage realMime = realSender.createMimeMessage();
        given(javaMailSender.createMimeMessage()).willReturn(realMime);
        given(templateEngine.process(eq("mail/verification"), any(Context.class)))
                .willReturn("<html>verification</html>");
    }

    private void stubRedisOps() {
        given(redisTemplate.opsForValue()).willReturn(valueOps);
    }

    @Test
    @DisplayName("sendVerificationCode: 성공 시 메일 전송 + Redis 3분 저장")
    void sendVerificationCode_success() throws Exception {
        // given
        stubMailBuilding();
        stubRedisOps();
        String email = "user@ex.com";
        SendCodeRequestDto req = new SendCodeRequestDto(email);

        // when
        mailService.sendVerificationCode(req);

        // then (Redis 저장된 코드 캡쳐)
        ArgumentCaptor<Object> codeCap = ArgumentCaptor.forClass(Object.class);
        verify(valueOps).set(eq(email), codeCap.capture(), eq(180L), eq(TimeUnit.SECONDS));
        String savedCode = String.valueOf(codeCap.getValue());
        assertThat(savedCode).isNotBlank().hasSize(8);

        // 템플릿 컨텍스트 확인
        ArgumentCaptor<Context> ctxCap = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("mail/verification"), ctxCap.capture());
        assertThat(ctxCap.getValue().getVariable("verificationCode")).isEqualTo(savedCode);

        // 메일 헤더 확인
        ArgumentCaptor<MimeMessage> msgCap = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender).send(msgCap.capture());
        MimeMessage msg = msgCap.getValue();
        assertThat(msg.getSubject()).isEqualTo("[오늘의 하루] 이메일 인증 번호 발송");

        Address[] froms = msg.getFrom();
        assertThat(froms).isNotEmpty();
        InternetAddress from = (InternetAddress) froms[0];
        assertThat(from.getAddress()).isEqualTo("noreply@ex.com");
        assertThat(from.getPersonal()).isEqualTo("오늘의 하루 관리자");

        Address[] tos = msg.getAllRecipients();
        assertThat(tos).isNotEmpty();
        assertThat(((InternetAddress) tos[0]).getAddress()).isEqualTo(email);
    }

    @Test
    @DisplayName("sendVerificationCode: 메일 전송 실패 시 MailSendException, Redis 저장 안 함")
    void sendVerificationCode_mailSendFail() {
        // given
        stubMailBuilding(); // 메시지 생성은 수행되므로 필요
        String email = "user@ex.com";
        SendCodeRequestDto req = new SendCodeRequestDto(email);
        // 전송 단계에서 실패
        willThrow(new RuntimeException("smtp down")).given(javaMailSender).send(any(MimeMessage.class));

        // when & then
        assertThatThrownBy(() -> mailService.sendVerificationCode(req))
                .isInstanceOf(MailSendException.class)
                .hasMessageContaining("이메일 전송 중 오류");

        // Redis는 아예 호출되지 않아야 함 (opsForValue() 스텁도 안 함)
        verify(valueOps, never()).set(eq(email), any(), anyLong(), any());
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("verifyCode: 코드 일치 시 통과")
    void verifyCode_success() {
        // given
        stubRedisOps();
        String email = "user@ex.com";
        String code = "Ab12Cd34";
        given(valueOps.get(email)).willReturn(code);

        // when & then
        EmailCondeValidationDto dto = new EmailCondeValidationDto(email, code);
        assertThatCode(() -> mailService.verifyCode(dto)).doesNotThrowAnyException();
        verify(valueOps).get(email);
    }

    @Test
    @DisplayName("verifyCode: 코드 불일치 시 EmailCodeNotMatchException")
    void verifyCode_notMatch() {
        // given
        stubRedisOps();
        String email = "user@ex.com";
        given(valueOps.get(email)).willReturn("RealCode");

        // when & then
        EmailCondeValidationDto dto = new EmailCondeValidationDto(email, "WrongCode");
        assertThatThrownBy(() -> mailService.verifyCode(dto))
                .isInstanceOf(EmailCodeNotMatchException.class)
                .hasMessageContaining("인증번호가 일치하지 않습니다.");
        verify(valueOps).get(email);
    }

    @Test
    @DisplayName("getCodeFromRedis: 만료(null) 시 EmailCodeExpireException")
    void getCodeFromRedis_expired() {
        // given
        stubRedisOps();
        String email = "user@ex.com";
        given(valueOps.get(email)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> mailService.getCodeFromRedis(email))
                .isInstanceOf(EmailCodeExpireException.class)
                .hasMessageContaining("만료");
        verify(valueOps).get(email);
    }
}
