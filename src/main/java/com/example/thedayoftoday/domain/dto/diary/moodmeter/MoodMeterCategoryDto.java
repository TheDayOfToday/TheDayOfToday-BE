package com.example.thedayoftoday.domain.dto.diary.moodmeter;

import java.util.List;

public record MoodMeterCategoryDto(
        String degree,
        List<MoodDetailsDto> moods
) implements MoodCategoryResponse {
}