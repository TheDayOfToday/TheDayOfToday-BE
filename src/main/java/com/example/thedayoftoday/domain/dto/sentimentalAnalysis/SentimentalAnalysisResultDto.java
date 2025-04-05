package com.example.thedayoftoday.domain.dto.sentimentalAnalysis;

import com.example.thedayoftoday.domain.entity.enumType.MoodMeter;

public record SentimentalAnalysisResultDto(
        String mood,
        MoodMeter moodMeter,
        String content
) {
}
