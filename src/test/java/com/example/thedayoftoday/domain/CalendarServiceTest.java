package com.example.thedayoftoday.domain;

import com.example.thedayoftoday.domain.dto.diary.DiaryEntryResponseDto;
import com.example.thedayoftoday.domain.dto.calendar.MonthColorsResponseDto;
import com.example.thedayoftoday.domain.dto.calendar.SentimentalAnalysisListResponseDto;
import com.example.thedayoftoday.domain.dto.calendar.SentimentalAnalysisResultDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.DiaryMood;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import com.example.thedayoftoday.domain.service.CalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private DiaryRepository diaryRepository;

    @InjectMocks
    private CalendarService calendarService;

    private Diary testDiary;

    @BeforeEach
    void setUp() {
        DiaryMood diaryMood = new DiaryMood("기쁨", "#FFD700"); // 기존 SentimentalAnalysis 대신 DiaryMood 사용

        testDiary = Diary.builder()
                .title("테스트 일기")
                .content("테스트 내용")
                .createTime(LocalDateTime.of(2025, 2, 15, 10, 0))
                .user(null)
                .diaryMood(diaryMood) // 변경된 DiaryMood 추가
                .analysisContent("매우 기쁨") // 감정 분석 내용 추가
                .build();
    }

    @Test
    void testGetMonthColors() {
        Long userId = 1L;
        LocalDateTime startDate = LocalDateTime.of(2025, 2, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 2, 28, 23, 59);

        when(diaryRepository.findByUser_UserIdAndCreateTimeBetween(userId, startDate, endDate))
                .thenReturn(Collections.singletonList(testDiary));

        MonthColorsResponseDto result = calendarService.getMonthColors(userId, startDate, endDate);

        assertEquals(userId, result.userId());
        assertNotNull(result.colors());
    }

    @Test
    void testGetDiaryEntry() {
        Long userId = 1L;
        LocalDateTime date = LocalDateTime.of(2025, 2, 15, 0, 0);

        when(diaryRepository.findByUser_UserIdAndCreateTimeBetween(anyLong(), any(), any()))
                .thenReturn(Collections.singletonList(testDiary));

        DiaryEntryResponseDto result = calendarService.getDiaryEntry(userId, date);

        assertEquals(userId, result.userId());
        assertEquals("2025-02-15", result.date());
        assertNotNull(result.entries());
        assertFalse(result.entries().isEmpty());
        assertEquals("테스트 일기", result.entries().get(0).title());
        assertEquals("테스트 내용", result.entries().get(0).content());
    }

    @Test
    void testGetSentimentalAnalysis() {
        Long userId = 1L;
        LocalDateTime date = LocalDateTime.of(2025, 2, 15, 0, 0);

        List<Diary> mockDiaries = Collections.singletonList(testDiary);

        when(diaryRepository.findByUser_UserIdAndCreateTimeBetween(eq(userId), any(), any()))
                .thenReturn(mockDiaries);

        SentimentalAnalysisListResponseDto result = calendarService.getSentimentalAnalysis(userId, date);

        assertEquals(userId, result.userId());
        assertEquals("2025-02-15", result.date());

        List<SentimentalAnalysisResultDto> analysisResults = result.analysisResults();
        assertNotNull(analysisResults);
        assertFalse(analysisResults.isEmpty());
        assertEquals("기쁨", analysisResults.get(0).mood());
        assertEquals("매우 기쁨", analysisResults.get(0).content());
    }
}
