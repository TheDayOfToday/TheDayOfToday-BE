package com.example.thedayoftoday.exception;

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
        ErrorCodeMessage errorCodeMessage = new ErrorCodeMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorCodeMessage);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorCodeMessage> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst().orElse("잘못된 요청입니다");
        ErrorCodeMessage errorCodeMessage = new ErrorCodeMessage(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorCodeMessage);
    }


}
