package com.example.thedayoftoday.domain.dto.diary;

import com.example.thedayoftoday.domain.entity.DiaryMood;

import java.time.LocalDate;

//늘 moodMeter, moodColor은 AI가 넣는것이 아닌 사람이 직접 주입
public record DiaryInfoResponseDto(
        String name,
        String title,
        String content,
        LocalDate createDate,
        DiaryMood diaryMood,
        String moodAnalysisContent
) {
}
