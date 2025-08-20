package thedayoftoday.domain.diary.exception;

public class DiaryAccessDeniedException extends RuntimeException {

    public DiaryAccessDeniedException() {
        super("해당 일기에 접근할 권한이 없습니다.");
    }

    public DiaryAccessDeniedException(String message) {
        super(message);
    }
}
