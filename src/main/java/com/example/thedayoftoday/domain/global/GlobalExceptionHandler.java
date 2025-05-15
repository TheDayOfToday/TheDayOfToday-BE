package com.example.thedayoftoday.domain.global;

import com.example.thedayoftoday.domain.exception.EmailCodeExpireException;
import com.example.thedayoftoday.domain.exception.EmailCodeNotMatchException;
import com.example.thedayoftoday.domain.exception.EmailDuplicationException;
import com.example.thedayoftoday.domain.exception.ErrorCodeMessage;
import com.example.thedayoftoday.domain.exception.MailSendException;
import com.example.thedayoftoday.domain.exception.PhoneNumberDuplicationExceptiono;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailDuplicationException.class)
    public ResponseEntity<ErrorCodeMessage> handleEmailDuplicate(EmailDuplicationException e) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        ErrorCodeMessage errorCodeMessage = new ErrorCodeMessage(httpStatus.value(), e.getMessage());
        return ResponseEntity.status(httpStatus).body(errorCodeMessage);
    }

    @ExceptionHandler(PhoneNumberDuplicationExceptiono.class)
    public ResponseEntity<ErrorCodeMessage> handlePhoneNumberDuplicate(PhoneNumberDuplicationExceptiono e) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        ErrorCodeMessage errorCodeMessage = new ErrorCodeMessage(httpStatus.value(), e.getMessage());
        return ResponseEntity.status(httpStatus).body(errorCodeMessage);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorCodeMessage> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst().orElse("잘못된 요청입니다");
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ErrorCodeMessage errorCodeMessage = new ErrorCodeMessage(httpStatus.value(), message);
        return ResponseEntity.status(httpStatus).body(errorCodeMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorCodeMessage> handleIllegalArgument(IllegalArgumentException e) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        ErrorCodeMessage errorCodeMessage = new ErrorCodeMessage(httpStatus.value(), e.getMessage());
        return ResponseEntity.status(httpStatus).body(errorCodeMessage);
    }

    @ExceptionHandler(EmailCodeNotMatchException.class)
    public ResponseEntity<ErrorCodeMessage> handleEmailCodeNotMatch(EmailCodeNotMatchException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ErrorCodeMessage errorCodeMessage = new ErrorCodeMessage(httpStatus.value(), e.getMessage());
        return ResponseEntity.status(httpStatus).body(errorCodeMessage);
    }

    @ExceptionHandler(EmailCodeExpireException.class)
    public ResponseEntity<ErrorCodeMessage> handleEmailCodeExpire(EmailCodeExpireException e) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        ErrorCodeMessage errorCodeMessage = new ErrorCodeMessage(httpStatus.value(), e.getMessage());
        return ResponseEntity.status(httpStatus).body(errorCodeMessage);
    }

    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<ErrorCodeMessage> handleMailSend(MailSendException e) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorCodeMessage errorCodeMessage = new ErrorCodeMessage(httpStatus.value(), e.getMessage());
        return ResponseEntity.status(httpStatus).body(errorCodeMessage);
    }
}
