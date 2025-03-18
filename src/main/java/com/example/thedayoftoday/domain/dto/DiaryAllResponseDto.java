package com.example.thedayoftoday.domain.dto;

import java.time.LocalDateTime;
//늘 moodMeter, moodColor은 AI가 넣는것이 아닌 사람이 직접 주입
public record DiaryAllResponseDto(
        String userName,
        String title,
        String content,
        LocalDateTime createTime,
        String moodName,
        String moodColor,
        String moodAnalysisContent
) {
}
