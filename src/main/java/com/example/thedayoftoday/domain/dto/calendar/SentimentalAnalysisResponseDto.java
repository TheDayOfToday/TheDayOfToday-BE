package com.example.thedayoftoday.domain.dto.calendar;

public record SentimentalAnalysisResponseDto(
        String analysisMoodName,
        String analysisMoodColor,
        String analysisContent
) {
}
