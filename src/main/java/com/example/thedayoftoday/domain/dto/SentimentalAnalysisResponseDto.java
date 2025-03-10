package com.example.thedayoftoday.domain.dto;

import com.example.thedayoftoday.domain.entity.enumType.MoodMeter;

public record SentimentalAnalysisResponseDto(
        String moodName,
        MoodMeter moodMeter,
        String content
) {
    public static SentimentalAnalysisResponseDto defaultAnalysis() {
        return new SentimentalAnalysisResponseDto("행복", MoodMeter.HAPPY, "기분 좋은 하루");
    }
}
