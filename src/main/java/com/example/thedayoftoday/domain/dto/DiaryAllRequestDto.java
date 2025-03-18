package com.example.thedayoftoday.domain.dto;

import com.example.thedayoftoday.domain.entity.DiaryMood;

public record DiaryAllRequestDto(
        String title,
        String content,
        DiaryMood diaryMood
) {
}
