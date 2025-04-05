package com.example.thedayoftoday.domain.dto.sentimentalAnalysis;

import java.util.List;

public record UnknownMoodCategoryDto(List<MoodDetailsDto> moods) implements MoodCategoryResponse {
}
