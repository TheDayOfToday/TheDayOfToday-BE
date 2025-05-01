package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.diary.AIAnalysisContentDto;
import com.example.thedayoftoday.domain.dto.diary.DiaryBasicResponseDto;
import com.example.thedayoftoday.domain.dto.calendar.MonthColorsResponseDto;
import com.example.thedayoftoday.domain.dto.calendar.SentimentalAnalysisListResponseDto;
import com.example.thedayoftoday.domain.dto.calendar.SentimentalAnalysisResultDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.DiaryMood;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CalendarService {
    private final DiaryRepository diaryRepository;

    public CalendarService(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }

    public MonthColorsResponseDto getMonthColors(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Diary> diaries = diaryRepository.findByUser_UserIdAndCreateTimeBetween(userId, startDate, endDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, String> colors = new HashMap<>();

        for (Diary diary : diaries) {
            String dateKey = diary.getCreateTime().format(formatter);
            String moodColor = "미분석";

            if (diary.getDiaryMood() != null && !diary.isEmpty()) {
                moodColor = diary.getDiaryMood().getMoodColor();
            }

            colors.put(dateKey, moodColor);
        }

        return new MonthColorsResponseDto(colors);
    }

    public DiaryBasicResponseDto getDiaryEntry(Long userId, LocalDateTime dateTime) {
        LocalDateTime startOfDay = dateTime.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        return diaryRepository.findByUser_UserIdAndCreateTimeBetween(userId, startOfDay, endOfDay).stream()
                .filter(diary -> !diary.isEmpty())
                .findFirst()
                .map(diary -> new DiaryBasicResponseDto(diary.getTitle(), diary.getContent()))
                .orElse(null);
    }

    public AIAnalysisContentDto getSentimentalAnalysis(Long userId, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        List<Diary> diaries = diaryRepository.findByUser_UserIdAndCreateTimeBetween(userId, startOfDay, endOfDay);
        String analysisContent = "해당 날짜의 감정 분석 데이터가 없습니다.";

        for (Diary diary : diaries) {
            if (!diary.isEmpty()) {
                analysisContent = diary.getAnalysisContent();
            }
        }
        return new AIAnalysisContentDto(analysisContent);
    }
}
