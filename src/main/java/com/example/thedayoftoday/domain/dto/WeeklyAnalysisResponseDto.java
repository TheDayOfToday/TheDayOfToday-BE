package com.example.thedayoftoday.domain.dto;

import com.example.thedayoftoday.domain.entity.enumType.MoodMeter;

import java.time.LocalDate;

public record   WeeklyAnalysisResponseDto(
        int year,
        int month,
        int week,
        String title,
        MoodMeter analysisMoodmeter,  // MoodMeter enum 적용
        String feedback,
        LocalDate startDate,  // LocalDate 적용
        LocalDate endDate,
        String message
) {
    public static WeeklyAnalysisResponseDto noData(int year, int month, int week) {
        return new WeeklyAnalysisResponseDto(year, month, week, null, null, null, null, null, "해당 주차에 대한 분석 데이터가 없습니다.");
    }
}
