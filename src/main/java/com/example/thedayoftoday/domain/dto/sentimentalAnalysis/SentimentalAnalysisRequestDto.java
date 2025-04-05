package com.example.thedayoftoday.domain.dto.sentimentalAnalysis;

public record SentimentalAnalysisRequestDto(
        String analysisMoodName,
        String analysisMoodColor,
        String analysisContent
) {
}
