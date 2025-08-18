package thedayoftoday.dto.diary;

import thedayoftoday.entity.DiaryMood;

public record DiaryRequestDto(Long diaryId, String title, String content, DiaryMood diaryMood) {
}
