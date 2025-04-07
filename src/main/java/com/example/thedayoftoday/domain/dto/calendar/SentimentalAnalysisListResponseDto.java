package com.example.thedayoftoday.domain.dto.calendar;

import java.util.List;

public record SentimentalAnalysisListResponseDto(
        String date,
        List<SentimentalAnalysisResultDto> analysisResults
) {
}
