package com.example.thedayoftoday.domain.dto.diary;

import com.example.thedayoftoday.domain.entity.DiaryMood;

public record DiaryRequestDto(Long diaryId, String title, String content, DiaryMood diaryMood) {}
