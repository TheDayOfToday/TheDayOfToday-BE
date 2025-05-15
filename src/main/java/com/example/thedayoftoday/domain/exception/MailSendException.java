package com.example.thedayoftoday.domain.exception;

public class MailSendException extends RuntimeException {
  public MailSendException(String message) {
    super(message);
  }
}
