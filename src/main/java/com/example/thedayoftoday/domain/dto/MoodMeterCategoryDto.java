package com.example.thedayoftoday.domain.dto;

import java.util.List;

public record MoodMeterCategoryDto(
        String degree,
        List<MoodDetailsDto> moods
) {
}