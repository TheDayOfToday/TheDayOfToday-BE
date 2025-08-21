package thedayoftoday.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import thedayoftoday.domain.auth.exception.PhoneNumberDuplicationExceptiono;
import thedayoftoday.domain.auth.mail.exception.EmailCodeExpireException;
import thedayoftoday.domain.auth.mail.exception.EmailCodeNotMatchException;
import thedayoftoday.domain.auth.mail.exception.EmailDuplicationException;
import thedayoftoday.domain.auth.mail.exception.MailSendException;
import thedayoftoday.domain.diary.exception.DiaryAccessDeniedException;
import thedayoftoday.domain.diary.exception.DiaryNotFoundException;
import thedayoftoday.domain.diary.exception.MoodNotSelectedException;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailDuplicationException.class)
    public ResponseEntity<ErrorCodeMessage> handleEmailDuplicate(EmailDuplicationException e) {
        return createErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(PhoneNumberDuplicationExceptiono.class)
    public ResponseEntity<ErrorCodeMessage> handlePhoneNumberDuplicate(PhoneNumberDuplicationExceptiono e) {
        return createErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorCodeMessage> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst().orElse("잘못된 요청입니다");
        return createErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorCodeMessage> handleIllegalArgument(IllegalArgumentException e) {
        return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(EmailCodeNotMatchException.class)
    public ResponseEntity<ErrorCodeMessage> handleEmailCodeNotMatch(EmailCodeNotMatchException e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(EmailCodeExpireException.class)
    public ResponseEntity<ErrorCodeMessage> handleEmailCodeExpire(EmailCodeExpireException e) {
        return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<ErrorCodeMessage> handleMailSend(MailSendException e) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(DiaryNotFoundException.class)
    public ResponseEntity<ErrorCodeMessage> handleDiaryNotFound(DiaryNotFoundException e) {
        return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DiaryAccessDeniedException.class)
    public ResponseEntity<ErrorCodeMessage> handleDiaryAccessDenied(DiaryAccessDeniedException e) {
        return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(MoodNotSelectedException.class)
    public ResponseEntity<ErrorCodeMessage> handleMoodNotSelected(MoodNotSelectedException e) {
        return createErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    private ResponseEntity<ErrorCodeMessage> createErrorResponse(HttpStatus status, String message) {
        ErrorCodeMessage errorCodeMessage = new ErrorCodeMessage(status.value(), message);
        return ResponseEntity.status(status).body(errorCodeMessage);
    }
}