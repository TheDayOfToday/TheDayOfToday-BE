package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.*;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import com.example.thedayoftoday.domain.repository.SentimentalAnalysisRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CalendarService {
    private final DiaryRepository diaryRepository;
    private final SentimentalAnalysisRepository sentimentalAnalysisRepository;

    public CalendarService(DiaryRepository diaryRepository, SentimentalAnalysisRepository sentimentalAnalysisRepository) {
        this.diaryRepository = diaryRepository;
        this.sentimentalAnalysisRepository = sentimentalAnalysisRepository;
    }

    public MonthColorsResponseDto getMonthColors(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Diary> diaries = diaryRepository.findByUser_UserIdAndCreateTimeBetween(userId, startDate, endDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, String> colors = diaries.stream()
                .collect(Collectors.toMap(
                        diary -> diary.getCreateTime().format(formatter),
                        diary -> diary.getSentimentAnalysis() != null ? diary.getSentimentAnalysis().getMoodName() : "미분석"
                ));

        return new MonthColorsResponseDto(userId, colors);
    }

    public DiaryEntryResponseDto getDiaryEntry(Long userId, LocalDateTime date) {
        List<Diary> diaries = diaryRepository.findByUser_UserIdAndCreateTimeBetween(
                userId,
                date.withHour(0).withMinute(0).withSecond(0),
                date.withHour(23).withMinute(59).withSecond(59)
        );

        List<DiaryContentDto> diaryEntries = diaries.stream()
                .map(diary -> new DiaryContentDto(diary.getTitle(), diary.getContent()))
                .collect(Collectors.toList());

        return new DiaryEntryResponseDto(
                userId,
                date.toLocalDate().toString(),
                diaryEntries.isEmpty() ? List.of(new DiaryContentDto("일기 없음", "해당 날짜에 작성된 일기가 없습니다.")) : diaryEntries
        );
    }

    public SentimentalAnalysisListResponseDto getSentimentalAnalysis(Long userId, LocalDateTime date) {
        List<Diary> diaries = diaryRepository.findByUser_UserIdAndCreateTimeBetween(
                userId,
                date.withHour(0).withMinute(0).withSecond(0),
                date.withHour(23).withMinute(59).withSecond(59)
        );

        List<SentimentalAnalysisResultDto> analysisResults = diaries.stream()
                .filter(diary -> diary.getSentimentAnalysis() != null)
                .map(diary -> new SentimentalAnalysisResultDto(
                        diary.getSentimentAnalysis().getMoodName(),
                        diary.getSentimentAnalysis().getMoodmeter(),
                        diary.getSentimentAnalysis().getContent()
                ))
                .collect(Collectors.toList());

        return new SentimentalAnalysisListResponseDto(
                userId,
                date.toLocalDate().toString(),
                analysisResults.isEmpty()
                        ? List.of(new SentimentalAnalysisResultDto("분석 없음", null, "해당 날짜의 감정 분석 데이터가 없습니다."))
                        : analysisResults
        );
    }
}
