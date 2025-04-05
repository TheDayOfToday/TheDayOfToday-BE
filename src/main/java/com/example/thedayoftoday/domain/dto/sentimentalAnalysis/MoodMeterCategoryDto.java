package com.example.thedayoftoday.domain.dto.sentimentalAnalysis;

import java.util.List;

public record MoodMeterCategoryDto(
        String degree,
        List<MoodDetailsDto> moods
) implements MoodCategoryResponse {
}