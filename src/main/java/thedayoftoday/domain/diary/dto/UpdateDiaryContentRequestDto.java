package thedayoftoday.domain.diary.dto;

public record UpdateDiaryContentRequestDto(Long diaryId, String title, String content) {
}
