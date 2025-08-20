package thedayoftoday.domain.diary.moodmeter;

import java.util.List;

public record MoodMeterCategoryDto(
        String degree,
        List<MoodDetailsDto> moods
) implements MoodCategoryResponse {
}