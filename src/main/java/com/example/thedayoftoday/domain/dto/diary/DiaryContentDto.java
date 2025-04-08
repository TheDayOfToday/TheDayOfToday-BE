package com.example.thedayoftoday.domain.dto.diary;

public record DiaryContentDto(
        Long diaryId,
        String title,
        String content
) {
}
