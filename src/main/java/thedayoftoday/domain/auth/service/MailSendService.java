package thedayoftoday.domain.auth.service;

import thedayoftoday.domain.auth.dto.SendCodeRequestDto;
import thedayoftoday.domain.auth.exception.EmailCodeExpireException;
import thedayoftoday.domain.auth.exception.MailSendException;
import jakarta.mail.MessagingException;
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

@Service
@Slf4j
public class MailSendService {
    private static String number;
    private final JavaMailSender javaMailSender;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public MailSendService(JavaMailSender javaMailSender, RedisTemplate<String, Object> redisTemplate) {
        this.javaMailSender = javaMailSender;
        this.redisTemplate = redisTemplate;
    }

    //email을 key값 code를 value로 하여 3분동안 저장한다.
    public void setCodeMaximumTimeFromRedis(String email,String code){
        ValueOperations<String, Object> valOperations = redisTemplate.opsForValue();
        //만료기간 3분
        valOperations.set(email,code,180, TimeUnit.SECONDS);
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

    public static void createNumber(){
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for(int i=0; i<8; i++) { // 총 8자리 인증 번호 생성
            int idx = random.nextInt(3);

            switch (idx) {
                case 0 :
                    key.append((char) (random.nextInt(26) + 97));
                    break;
                case 1:
                    key.append((char) (random.nextInt(26) + 65));
                    break;
                case 2:
                    key.append(random.nextInt(9));
                    break;
            }
        }
        number = key.toString();
    }

    public MimeMessage createPasswordChangeMessage(String email){
        createNumber();
        log.info("Number : {}",number);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try{
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true); // Helper 사용
            messageHelper.setFrom(senderEmail);
            messageHelper.setTo(email);
            messageHelper.setSubject("[오늘의 하루] 이메일 인증 번호 발송");
            String body = "<html><body style='background-color: #000000; margin: 0 auto; max-width: 600px; word-break: break-word; padding: 50px 30px; color: #ffffff; font-family: sans-serif;'>";

            body += "<h1 style='padding-top: 40px; font-size: 30px; margin-bottom: 30px;'>이메일 주소 인증</h1>";

            body += "<p style='font-size: 18px; line-height: 32px; margin-bottom: 24px;'>안녕하세요? 오늘의 하루입니다.</p>";
            body += "<p style='font-size: 18px; line-height: 32px; margin-bottom: 24px;'>하단의 인증 번호로 이메일 인증을 완료하시면, 정상적으로 오늘의 하루 서비스를 이용하실 수 있습니다.</p>";
            body += "<p style='font-size: 18px; line-height: 32px; margin-bottom: 24px;'>항상 최선의 노력을 다하는 오늘의 하루가 되겠습니다.</p>";
            body += "<p style='font-size: 18px; line-height: 32px;'>감사합니다.</p>";

            body += "<div style='margin-top: 40px; padding: 20px 0; color: #000000; font-size: 25px; text-align: center; background-color: #f4f4f4; border-radius: 10px; font-weight: bold;'>" + number + "</div>";

            body += "</body></html>";   messageHelper.setText(body, true);
        }catch (MessagingException e){
            throw new MailSendException("이메일 전송 중 오류가 발생하였습니다");
        }
        return mimeMessage;
    }

    public String sendMail(String email) {
        MimeMessage mimeMessage = createPasswordChangeMessage(email);
        log.info("[Mail 전송 시작]");
        javaMailSender.send(mimeMessage);
        log.info("[Mail 전송 완료]");
        return number;
    }

    public String sendCode(SendCodeRequestDto sendCodeRequestDto) {
        String email = sendCodeRequestDto.email();
        String code = sendMail(email);
        setCodeMaximumTimeFromRedis(email, code);
        return code;
    }
}
