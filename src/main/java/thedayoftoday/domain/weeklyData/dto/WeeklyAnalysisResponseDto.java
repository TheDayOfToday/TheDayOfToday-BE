package thedayoftoday.domain.weeklyData.dto;

import thedayoftoday.domain.weeklyData.entity.Degree;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

public record WeeklyAnalysisResponseDto(
        int year,
        int month,
        int day,
        String title,
        Degree degree,  // MoodMeter enum 적용
        String feedback,
        LocalDate startDate,  // LocalDate 적용
        LocalDate endDate
) {
    public static WeeklyAnalysisResponseDto noData(int year, int month, int day) {
        LocalDate date = LocalDate.of(year, month, day);
        WeekFields weekFields = WeekFields.ISO;
        int dayOfWeek = date.get(weekFields.dayOfWeek());
        LocalDate start = date.minusDays(dayOfWeek - 1);
        LocalDate end = start.plusDays(6);
        return new WeeklyAnalysisResponseDto(year, month, day, null, null, null, start, end);
    }
}
