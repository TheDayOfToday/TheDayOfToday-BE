package com.example.thedayoftoday.domain.exception;

public class EmailCodeExpireException extends RuntimeException {
    public EmailCodeExpireException(String message) {
        super(message);
    }
}
