package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.entity.WeeklyData;
import com.example.thedayoftoday.domain.repository.WeeklyDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyAnalysisServiceTest {

    @Mock
    private WeeklyDataRepository weeklyDataRepository;

    @InjectMocks
    private WeeklyAnalysisService weeklyAnalysisService;

    private WeeklyData sampleData;

    @BeforeEach
    void setUp() {
        sampleData = WeeklyData.builder()
                .title("2주차 감정 분석")
                .feedback("긍정긍정 맨~")
                .startDate(LocalDate.of(2025, 11, 4))  // 2주차 시작
                .endDate(LocalDate.of(2025, 11, 10))   // 2주차 끝
                .build();
    }

    @Test
    void testGetWeeklyAnalysis_Success() {
        when(weeklyDataRepository.findAll()).thenReturn(List.of(sampleData));

        Map<String, Object> result = weeklyAnalysisService.getWeeklyAnalysis(2025, 11, 2);

        assertEquals(2025, result.get("year"));
        assertEquals(11, result.get("month"));
        assertEquals(2, result.get("week"));
        assertEquals("2주차 감정 분석", result.get("title"));
        assertEquals("긍정긍정 맨~", result.get("feedback"));

        verify(weeklyDataRepository, times(1)).findAll();
    }

    @Test
    void testGetWeeklyAnalysis_NoData() {
        when(weeklyDataRepository.findAll()).thenReturn(List.of());

        Map<String, Object> result = weeklyAnalysisService.getWeeklyAnalysis(2025, 11, 2);

        assertEquals(2025, result.get("year"));
        assertEquals(11, result.get("month"));
        assertEquals(2, result.get("week"));
        assertEquals("해당 주차에 대한 분석 데이터가 없습니다.", result.get("message"));

        verify(weeklyDataRepository, times(1)).findAll();
    }
}
