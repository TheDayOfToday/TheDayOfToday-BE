package thedayoftoday.domain.auth.mail.exception;

public class EmailDuplicationException extends RuntimeException {
    public EmailDuplicationException(String message) {
        super(message);
    }
}
