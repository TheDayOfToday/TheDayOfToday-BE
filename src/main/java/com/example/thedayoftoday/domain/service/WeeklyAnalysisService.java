package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.weeklyAnalysis.WeeklyAnalysisResponseDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.WeeklyData;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import com.example.thedayoftoday.domain.repository.WeeklyDataRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WeeklyAnalysisService {

    private final WeeklyDataRepository weeklyDataRepository;
    private final DiaryRepository diaryRepository;

    public WeeklyAnalysisService(WeeklyDataRepository weeklyDataRepository, DiaryRepository diaryRepository) {
        this.weeklyDataRepository = weeklyDataRepository;
        this.diaryRepository = diaryRepository;
    }

    public LocalDate[] calculateStartAndEndDate(int year, int month, int week) {
        WeekFields weekFields = WeekFields.ISO;
        LocalDate baseDate = LocalDate.of(year, month, 1);
        LocalDate firstMonday = baseDate.with(weekFields.dayOfWeek(), 1);
        LocalDate startDate = firstMonday.plusWeeks(week - 1);
        LocalDate endDate = startDate.plusDays(6);
        return new LocalDate[]{startDate, endDate};
    }

    public WeeklyAnalysisResponseDto getWeeklyAnalysis(Long userId, int year, int month, int week) {
        LocalDate[] weekRange = calculateStartAndEndDate(year, month, week);
        LocalDate startDate = weekRange[0];
        LocalDate endDate = weekRange[1];

        WeeklyData targetWeekData = weeklyDataRepository
                .findByUser_UserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(userId, endDate, startDate)
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

    public List<Diary> extractedWeeklyDiaryData(long userId, int year, int month, int week) {
        LocalDate[] weekRange = calculateStartAndEndDate(year, month, week);
        LocalDate startDateTime = weekRange[0];
        LocalDate endDateTime = weekRange[1];

        return diaryRepository.findByUser_UserIdAndCreateTimeBetween(userId, startDateTime, endDateTime);
    }

    public String combineWeeklyDiary(List<Diary> diaries) {
        return diaries.stream()
                .map(diary -> {
                    String moodName = diary.getDiaryMood() != null ? diary.getDiaryMood().getMoodName() : "저장된 감정 없음";
                    return "[기분: " + moodName + "]\n" + diary.getContent();
                })
                .collect(Collectors.joining("\n\n"));
    }
}
