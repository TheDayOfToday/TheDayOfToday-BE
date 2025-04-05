package com.example.thedayoftoday.domain.dto.diary.moodmeter;

import java.util.List;

public record UnknownMoodCategoryDto(List<MoodDetailsDto> moods) implements MoodCategoryResponse {
}
