package thedayoftoday.domain.auth.mail.exception;

public class EmailCodeExpireException extends RuntimeException {
    public EmailCodeExpireException(String message) {
        super(message);
    }
}
