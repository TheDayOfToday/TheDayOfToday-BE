package com.example.thedayoftoday.domain.dto.calendar;

public record SentimentalAnalysisRequestDto(
        String analysisMoodName,
        String analysisMoodColor,
        String analysisContent
) {
}
