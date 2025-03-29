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
import java.util.stream.Collectors;
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
                targetWeekData.getDegree(),
                targetWeekData.getFeedback(),
                targetWeekData.getStartDate(),
                targetWeekData.getEndDate())
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

    public List<Diary> getWeeklyDiary(long userId, int year, int month, int week) {

        WeekFields weekFields = WeekFields.ISO; //월요일 기준으로 잡음

        LocalDate baseDate = LocalDate.of(year, month, 1);

        LocalDate startOfWeek = baseDate
                .with(weekFields.weekOfMonth(), week)
                .with(weekFields.dayOfWeek(), 1); // ISO 기준: 1은 월요일

        LocalDate endOfWeek = startOfWeek.plusDays(6);

        LocalDateTime startDateTime = startOfWeek.atStartOfDay();
        LocalDateTime endDateTime = endOfWeek.atTime(LocalTime.MAX);

        return diaryRepository.findByUser_UserIdAndCreateTimeBetween(userId, startDateTime, endDateTime);
    }
    //만약 2.1(수) 이렇게 되어있으면 1월 마지막주, 2월 첫째주 다 들어감

    public String combineWeeklyDiary(List<Diary> diaries) {
        return diaries.stream()
                .map(diary -> {
                    String moodName = diary.getDiaryMood() != null ? diary.getDiaryMood().getMoodName() : "저장된 감정 없음";
                    return "[기분: " + moodName + "]\n" + diary.getContent();
                })
                .collect(Collectors.joining("\n\n"));
    }

}
