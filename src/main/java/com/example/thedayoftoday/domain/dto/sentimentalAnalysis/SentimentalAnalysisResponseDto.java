package com.example.thedayoftoday.domain.dto.sentimentalAnalysis;

public record SentimentalAnalysisResponseDto(
        String analysisMoodName,
        String analysisMoodColor,
        String analysisContent
) {
}
