package com.example.thedayoftoday.domain.dto;

import java.util.List;

public record SentimentalAnalysisListResponseDto(
        Long userId,
        String date,
        List<SentimentalAnalysisResultDto> analysisResults
) {
}
