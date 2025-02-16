package com.example.thedayoftoday.domain;

import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.SentimentalAnalysis;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import com.example.thedayoftoday.domain.repository.SentimentalAnalysisRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CalendarService {
    private final DiaryRepository diaryRepository;
    private final SentimentalAnalysisRepository sentimentalAnalysisRepository;

    public CalendarService(DiaryRepository diaryRepository, SentimentalAnalysisRepository sentimentalAnalysisRepository) {
        this.diaryRepository = diaryRepository;
        this.sentimentalAnalysisRepository = sentimentalAnalysisRepository;
    }

    public Map<String, Object> getMonthColors(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Diary> diaries = diaryRepository.findByUser_UserIdAndCreateTimeBetween(userId, startDate, endDate);
        Map<String, Object> response = new HashMap<>();

        Map<String, String> colors = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Diary diary : diaries) {
            String dateKey = diary.getCreateTime().format(formatter);
            colors.put(dateKey, diary.getSentimentAnalysis() != null ? diary.getSentimentAnalysis().getMoodName() : "미분석");
        }

        response.put("userId", userId);
        response.put("colors", colors);
        return response;
    }

    public Map<String, Object> getDiaryEntry(Long userId, LocalDateTime date) {
        List<Diary> diaries = diaryRepository.findByUser_UserIdAndCreateTimeBetween(
                userId,
                date.withHour(0).withMinute(0).withSecond(0),
                date.withHour(23).withMinute(59).withSecond(59)
        );

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("date", date.toLocalDate().toString());

        if (!diaries.isEmpty()) {
            List<Map<String, Object>> diaryEntries = new ArrayList<>();
            for (Diary diary : diaries) {
                Map<String, Object> diaryData = new HashMap<>();
                diaryData.put("title", diary.getTitle());
                diaryData.put("content", diary.getContent());
                diaryEntries.add(diaryData);
            }
            response.put("entries", diaryEntries);
        } else {
            response.put("entries", "일기가 없습니다.");
        }

        return response;
    }

    public Map<String, Object> getSentimentalAnalysis(Long userId, LocalDateTime date) {
        List<Diary> diaries = diaryRepository.findByUser_UserIdAndCreateTimeBetween(
                userId,
                date.withHour(0).withMinute(0).withSecond(0),
                date.withHour(23).withMinute(59).withSecond(59)
        );

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("date", date.toLocalDate().toString());

        if (diaries.isEmpty()) {
            response.put("analysisResult", "해당 날짜에 분석된 데이터가 없습니다.");
            return response;
        }

        List<Map<String, Object>> analysisResults = new ArrayList<>();
        for (Diary diary : diaries) {
            if (diary.getSentimentAnalysis() != null) {
                Map<String, Object> analysisData = new HashMap<>();
                analysisData.put("mood", diary.getSentimentAnalysis().getMoodName());
                analysisData.put("moodmeter", diary.getSentimentAnalysis().getMoodmeter());
                analysisData.put("content", diary.getSentimentAnalysis().getContent());
                analysisResults.add(analysisData);
            }
        }

        if (analysisResults.isEmpty()) {
            response.put("analysisResult", "해당 날짜의 감정 분석 데이터가 없습니다.");
        } else {
            response.put("analysisResults", analysisResults);
        }

        return response;
    }
}
