package com.example.thedayoftoday.domain.dto;

public record SentimentalAnalysisResponseDto(
        String analysisMoodName,
        String analysisMoodColor,
        String analysisContent
) {
}
