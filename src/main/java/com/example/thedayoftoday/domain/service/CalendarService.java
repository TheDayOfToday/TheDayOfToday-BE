package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.diary.DiaryContentDto;
import com.example.thedayoftoday.domain.dto.diary.DiaryEntryResponseDto;
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
import java.util.stream.Collectors;
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

            if (diary.getDiaryMood() != null) {
                moodColor = diary.getDiaryMood().getMoodColor(); // MoodMeter 타입이라면 String 변환 필요
            }

            colors.put(dateKey, moodColor);
        }

        return new MonthColorsResponseDto(userId, colors);
    }


    public DiaryEntryResponseDto getDiaryEntry(Long userId, LocalDateTime date) {
        List<Diary> diaries = diaryRepository.findByUser_UserIdAndCreateTimeBetween(
                userId,
                date.withHour(0).withMinute(0).withSecond(0),
                date.withHour(23).withMinute(59).withSecond(59)
        );

        List<DiaryContentDto> diaryEntries = diaries.stream()
                .map(diary -> new DiaryContentDto(diary.getDiaryId(),diary.getTitle(), diary.getContent()))
                .collect(Collectors.toList());

        if (diaryEntries.isEmpty()) {
            diaryEntries.add(new DiaryContentDto(null,"일기 없음", "해당 날짜에 작성된 일기가 없습니다."));
        }

        return new DiaryEntryResponseDto(
                date.toLocalDate().toString(),
                diaryEntries
        );
    }

    public SentimentalAnalysisListResponseDto getSentimentalAnalysis(Long userId, LocalDateTime date) {
        List<Diary> diaries = diaryRepository.findByUser_UserIdAndCreateTimeBetween(
                userId,
                date.withHour(0).withMinute(0).withSecond(0),
                date.withHour(23).withMinute(59).withSecond(59)
        );

        List<SentimentalAnalysisResultDto> analysisResults = new ArrayList<>();

        for (Diary diary : diaries) {
            DiaryMood mood = null;
            String analysisContent = "해당 날짜의 감정 분석 데이터가 없습니다.";

            if (diary.getDiaryMood() != null) {
                mood=diary.getDiaryMood();
            }
            if (diary.getAnalysisContent() != null) {
                analysisContent = diary.getAnalysisContent();
            }

            analysisResults.add(new SentimentalAnalysisResultDto(mood, analysisContent));
        }

        if (analysisResults.isEmpty()) {
            analysisResults.add(new SentimentalAnalysisResultDto(null, "해당 날짜의 감정 분석 데이터가 없습니다."));
        }

        return new SentimentalAnalysisListResponseDto(date.toLocalDate().toString(), analysisResults);
    }
}
