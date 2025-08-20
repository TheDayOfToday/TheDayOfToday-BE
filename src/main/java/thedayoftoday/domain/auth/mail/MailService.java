package thedayoftoday.domain.auth.mail;

import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import thedayoftoday.domain.auth.mail.exception.EmailCodeExpireException;
import thedayoftoday.domain.auth.mail.exception.EmailCodeNotMatchException;
import thedayoftoday.domain.auth.mail.exception.MailSendException;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendVerificationCode(SendCodeRequestDto sendCodeRequestDto) {
        sendCode(sendCodeRequestDto);
    }

    public void sendCode(SendCodeRequestDto sendCodeRequestDto) {
        String email = sendCodeRequestDto.email();
        String code = createVerificationCode();
        sendMail(email, code);
        setCodeMaximumTimeFromRedis(email, code);
    }

    private String createVerificationCode(){
        Random random = new Random();
        StringBuilder key = new StringBuilder();

        for(int i=0; i<8; i++) {
            int idx = random.nextInt(3);
            switch (idx) {
                case 0: key.append((char) (random.nextInt(26) + 97)); break; // a~z
                case 1: key.append((char) (random.nextInt(26) + 65)); break; // A~Z
                case 2: key.append(random.nextInt(10)); break; // 0~9
            }
        }
        return key.toString();
    }

    public void verifyCode(EmailCondeValidationDto emailCondeValidationDto) {
        String code = getCodeFromRedis(emailCondeValidationDto.email());
        if (!code.equals(emailCondeValidationDto.code())) {
            throw new EmailCodeNotMatchException("인증번호가 일치하지 않습니다.");
        }
    }

    //key값인 email에 있는 value를 가져온다.
    public String getCodeFromRedis(String email){
        ValueOperations<String, Object> valOperations = redisTemplate.opsForValue();
        Object code = valOperations.get(email);
        if(code == null){
            throw new EmailCodeExpireException("인증번호가 만료되었습니다.");
        }
        return code.toString();
    }

    //email을 key값 code를 value로 하여 3분동안 저장한다.
    public void setCodeMaximumTimeFromRedis(String email,String code){
        ValueOperations<String, Object> valOperations = redisTemplate.opsForValue();
        //만료기간 3분
        valOperations.set(email,code,180, TimeUnit.SECONDS);
    }

    private void sendMail(String email, String code) {
        MimeMessage mimeMessage = createVerificationMessage(email, code);
        try {
            log.info("[Mail 전송 시작] To: {}", email);
            javaMailSender.send(mimeMessage);
            log.info("[Mail 전송 완료]");
        } catch (Exception e) {
            log.error("메일 전송 실패: {}", e.getMessage());
            throw new MailSendException("이메일 전송 중 오류가 발생했습니다.");
        }
    }

    private MimeMessage createVerificationMessage(String email, String code){
        log.info("인증번호 생성: {}", code);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try{
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setFrom(new InternetAddress(senderEmail, "오늘의 하루 관리자"));

            messageHelper.setTo(email);
            messageHelper.setSubject("[오늘의 하루] 이메일 인증 번호 발송");

            Context context = new Context();
            context.setVariable("verificationCode", code);
            String htmlBody = templateEngine.process("mail/verification", context);

            messageHelper.setText(htmlBody, true);

        } catch (MessagingException | UnsupportedEncodingException e){
            log.error("MimeMessage 생성 실패: {}", e.getMessage());
            throw new MailSendException("이메일 메시지 생성 중 오류가 발생했습니다.");
        }
        return mimeMessage;
    }
}
