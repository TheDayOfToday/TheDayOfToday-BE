package com.example.thedayoftoday.domain.dto.calendar;

import com.example.thedayoftoday.domain.entity.DiaryMood;

public record SentimentalAnalysisResultDto(
        DiaryMood diaryMood,
        String content
) {
}
