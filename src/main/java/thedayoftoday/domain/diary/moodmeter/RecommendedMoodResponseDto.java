package thedayoftoday.domain.diary.moodmeter;

import java.util.List;

public record RecommendedMoodResponseDto(DiaryMood diaryMood, List<MoodCategoryResponse> moodCategories) {
}
