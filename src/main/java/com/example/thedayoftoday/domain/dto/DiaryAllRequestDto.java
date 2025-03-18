package com.example.thedayoftoday.domain.dto;

public record DiaryAllRequestDto(
        String title,
        String content,
        String moodName,
        String moodColor
) {
}
