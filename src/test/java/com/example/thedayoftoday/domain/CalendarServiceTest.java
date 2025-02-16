package com.example.thedayoftoday.domain;

import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.SentimentalAnalysis;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import com.example.thedayoftoday.domain.repository.SentimentalAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private SentimentalAnalysisRepository sentimentalAnalysisRepository;

    @InjectMocks
    private CalendarService calendarService;

    private Diary testDiary;
    private SentimentalAnalysis testAnalysis;

    @BeforeEach
    void setUp() {
        testDiary = Diary.builder()
                .title("테스트")
                .content("테스트 내용")
                .createTime(LocalDateTime.of(2025, 2, 15, 10, 0))
                .user(null) // Mocking, so no need for actual user
                .sentimentAnalysis(null)
                .build();

        testAnalysis = SentimentalAnalysis.builder()
                .moodName("기쁨")
                .content("매우 기쁨기쁨기쁨")
                .diary(testDiary)
                .build();
    }

    @Test
    void testGetMonthColors() {
        Long userId = 1L;
        LocalDateTime startDate = LocalDateTime.of(2025, 2, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 2, 28, 23, 59);

        when(diaryRepository.findByUser_UserIdAndCreateTimeBetween(userId, startDate, endDate))
                .thenReturn(Collections.singletonList(testDiary));

        Map<String, Object> result = calendarService.getMonthColors(userId, startDate, endDate);

        assertEquals(userId, result.get("userId"));
        assertNotNull(result.get("colors"));
    }

    @Test
    void testGetDiaryEntry() {
        Long userId = 1L;
        LocalDateTime date = LocalDateTime.of(2025, 2, 15, 0, 0);

        when(diaryRepository.findByUser_UserIdAndCreateTimeBetween(anyLong(), any(), any()))
                .thenReturn(Collections.singletonList(testDiary));

        Map<String, Object> result = calendarService.getDiaryEntry(userId, date);

        assertEquals(userId, result.get("userId"));
        assertNotNull(result.get("entries"));
    }

    @Test
    void testGetSentimentalAnalysis() {
        Long diaryId = 1L;

        when(diaryRepository.findSentimentAnalysisByDiaryId(diaryId))
                .thenReturn(Optional.of(testAnalysis));

        Map<String, Object> result = calendarService.getSentimentalAnalysis(diaryId);

        assertEquals("기쁨", result.get("mood"));
        assertEquals("매우 기쁨기쁨기쁨", result.get("content"));
    }
}
