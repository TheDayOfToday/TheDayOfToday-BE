package com.example.thedayoftoday.domain.dto;

import com.example.thedayoftoday.domain.entity.DiaryMood;

public record DiaryCreateRequestDto(
        String title,
        String content,
        DiaryMood diaryMood
) {
}
