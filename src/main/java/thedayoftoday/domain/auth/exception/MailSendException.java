package thedayoftoday.domain.auth.exception;

public class MailSendException extends RuntimeException {
  public MailSendException(String message) {
    super(message);
  }
}
