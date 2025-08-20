package thedayoftoday.domain.auth.exception;

public class EmailCodeExpireException extends RuntimeException {
    public EmailCodeExpireException(String message) {
        super(message);
    }
}
