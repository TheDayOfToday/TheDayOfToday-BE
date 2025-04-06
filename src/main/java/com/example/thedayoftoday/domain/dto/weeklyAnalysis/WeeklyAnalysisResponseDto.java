package com.example.thedayoftoday.domain.dto.weeklyAnalysis;

import com.example.thedayoftoday.domain.entity.enumType.Degree;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

public record   WeeklyAnalysisResponseDto(
        int year,
        int month,
        int week,
        String title,
        Degree degree,  // MoodMeter enum 적용
        String feedback,
        LocalDate startDate,  // LocalDate 적용
        LocalDate endDate
) {
    public static WeeklyAnalysisResponseDto noData(int year, int month, int week) {
        LocalDate base = LocalDate.of(year, month, 1);
        WeekFields weekFields = WeekFields.ISO;
        LocalDate start = base.with(weekFields.weekOfMonth(), week).with(weekFields.dayOfWeek(), 1);
        LocalDate end = start.plusDays(6);
        return new WeeklyAnalysisResponseDto(year, month, week, null, null, null, start, end);    }
}
