package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.WeeklyAnalysisResponseDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.WeeklyData;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import com.example.thedayoftoday.domain.repository.WeeklyDataRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WeeklyAnalysisService {

    private final WeeklyDataRepository weeklyDataRepository;
    private final DiaryRepository diaryRepository;

    public WeeklyAnalysisService(WeeklyDataRepository weeklyDataRepository, DiaryRepository diaryRepository) {
        this.weeklyDataRepository = weeklyDataRepository;
        this.diaryRepository = diaryRepository;
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
                targetWeekData.getAnalysisMoodmeter(),
                targetWeekData.getFeedback(),
                targetWeekData.getStartDate(),
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

    public List<Diary> getWeeklyData(long userId, int year, int month, int week) {

        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        WeekFields weekFields = WeekFields.ISO;

        LocalDate startOfWeek = firstDayOfMonth
                .with(weekFields.weekOfMonth(), week)
                .with(weekFields.dayOfWeek(), 1); //월요일인거 알려줌

        LocalDate endOfWeek = startOfWeek.with(weekFields.dayOfWeek(), 7); //일요일

        LocalDateTime startDateTime = startOfWeek.atStartOfDay();
        LocalDateTime endDateTime = endOfWeek.atTime(LocalTime.MAX);

        return diaryRepository.findByUser_UserIdAndCreateTimeBetween(userId, startDateTime, endDateTime);
    }

}
