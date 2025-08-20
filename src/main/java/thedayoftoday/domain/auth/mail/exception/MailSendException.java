package thedayoftoday.domain.auth.mail.exception;

public class MailSendException extends RuntimeException {
  public MailSendException(String message) {
    super(message);
  }
}
