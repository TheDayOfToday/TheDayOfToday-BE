package com.example.thedayoftoday.domain.dto;

import com.example.thedayoftoday.domain.dto.diary.DiaryBasicResponseDto;
import com.example.thedayoftoday.domain.entity.DiaryMood;

public record TestDto(String transcribeText, DiaryMood mood, DiaryBasicResponseDto diaryBasicResponseDto) {
}
