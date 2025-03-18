package com.example.thedayoftoday.domain.dto;

public record SentimentalAnalysisRequestDto(
        String analysisMoodName,
        String analysisMoodColor,
        String analysisContent
) {
}
