package thedayoftoday.domain.diary.exception;

public class DiaryNotFoundException extends RuntimeException {

    public DiaryNotFoundException(Long diaryId) {
        super("해당 일기를 찾을 수 없습니다. ID: " + diaryId);
    }

    public DiaryNotFoundException(String message) {
        super(message);
    }
}
