package thedayoftoday.dto.diary;

public record DiaryContentDto(
        Long diaryId,
        String title,
        String content
) {
}
