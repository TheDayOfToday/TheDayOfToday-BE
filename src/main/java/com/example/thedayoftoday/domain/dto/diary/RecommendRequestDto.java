package com.example.thedayoftoday.domain.dto.diary;

import com.example.thedayoftoday.domain.dto.diary.moodmeter.MoodCategoryResponse;
import com.example.thedayoftoday.domain.entity.DiaryMood;
import java.util.List;

public record RecommendRequestDto(DiaryMood diaryMood, List<MoodCategoryResponse> moodCategories) {
}
