package thedayoftoday.dto.diary;

import thedayoftoday.dto.diary.moodmeter.MoodCategoryResponse;
import thedayoftoday.entity.DiaryMood;

import java.util.List;

public record RecommendRequestDto(DiaryMood diaryMood, List<MoodCategoryResponse> moodCategories) {
}
