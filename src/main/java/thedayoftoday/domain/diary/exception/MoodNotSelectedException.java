package thedayoftoday.domain.diary.exception;

public class MoodNotSelectedException extends RuntimeException {

    public MoodNotSelectedException() {
        super("감정이 먼저 선택되어야 분석할 수 있습니다.");
    }

    public MoodNotSelectedException(String message) {
        super(message);
    }
}
