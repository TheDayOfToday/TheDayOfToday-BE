package com.example.thedayoftoday.domain.dto;

import com.example.thedayoftoday.domain.entity.enumType.MoodMeter;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SentimentalAnalysisResponseDto {

    private String moodName;

    private MoodMeter moodmeter;

    private String content;
}
