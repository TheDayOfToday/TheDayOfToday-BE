package thedayoftoday.domain.auth.exception;

public class EmailCodeNotMatchException extends RuntimeException {
  public EmailCodeNotMatchException(String message) {
    super(message);
  }
}
