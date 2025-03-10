package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.WeeklyAnalysisResponseDto;
import com.example.thedayoftoday.domain.entity.WeeklyData;
import com.example.thedayoftoday.domain.repository.WeeklyDataRepository;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

@Service
public class WeeklyAnalysisService {
    private final WeeklyDataRepository weeklyDataRepository;

    public WeeklyAnalysisService(WeeklyDataRepository weeklyDataRepository) {
        this.weeklyDataRepository = weeklyDataRepository;
    }

    public WeeklyAnalysisResponseDto getWeeklyAnalysis(int year, int month, int week) {
        List<WeeklyData> weeklyDataList = weeklyDataRepository.findAll();

        WeeklyData targetWeekData = weeklyDataList.stream()
                .filter(data -> isMatchingWeek(data, year, month, week))
                .findFirst()
                .orElse(null);

        return (targetWeekData != null)
                ? new WeeklyAnalysisResponseDto(
                year, month, week,
                targetWeekData.getTitle(),
                targetWeekData.getAnalysisMoodmeter(),  // MoodMeter enum 그대로 전달
                targetWeekData.getFeedback(),
                targetWeekData.getStartDate(),  // LocalDate 그대로 전달
                targetWeekData.getEndDate(),
                null)
                : WeeklyAnalysisResponseDto.noData(year, month, week);
    }

    private boolean isMatchingWeek(WeeklyData data, int year, int month, int week) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(java.sql.Date.valueOf(data.getStartDate()));
        int dataYear = calendar.get(Calendar.YEAR);
        int dataMonth = calendar.get(Calendar.MONTH) + 1;
        int dataWeek = calendar.get(Calendar.WEEK_OF_MONTH);

        return dataYear == year && dataMonth == month && dataWeek == week;
    }
}
