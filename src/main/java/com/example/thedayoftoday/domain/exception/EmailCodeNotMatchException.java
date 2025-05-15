package com.example.thedayoftoday.domain.exception;

public class EmailCodeNotMatchException extends RuntimeException {
  public EmailCodeNotMatchException(String message) {
    super(message);
  }
}
