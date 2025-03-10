package com.example.thedayoftoday.domain.dto;

import com.example.thedayoftoday.domain.entity.enumType.MoodMeter;
import java.time.LocalDateTime;

public record DiaryResponseDto(
        String nickName,
        String diaryTitle,
        String diaryContent,
        LocalDateTime createTime,
        String moodName,
        MoodMeter moodMeter,
        String analysisContent
) {
}
