package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.WeeklyAnalysisResponseDto;
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

        WeeklyAnalysisResponseDto result = weeklyAnalysisService.getWeeklyAnalysis(2025, 11, 2);

        assertEquals(2025, result.year());
        assertEquals(11, result.month());
        assertEquals(2, result.week());
        assertEquals("2주차 감정 분석", result.title());
        assertEquals("긍정긍정 맨~", result.feedback());
        assertEquals(LocalDate.of(2025, 11, 4), result.startDate());
        assertEquals(LocalDate.of(2025, 11, 10), result.endDate());

        verify(weeklyDataRepository, times(1)).findAll();
    }

    @Test
    void testGetWeeklyAnalysis_NoData() {
        when(weeklyDataRepository.findAll()).thenReturn(List.of());

        WeeklyAnalysisResponseDto result = weeklyAnalysisService.getWeeklyAnalysis(2025, 11, 2);

        assertEquals(2025, result.year());
        assertEquals(11, result.month());
        assertEquals(2, result.week());
        assertNull(result.title());
        assertNull(result.feedback());
        assertNull(result.startDate());
        assertNull(result.endDate());

        verify(weeklyDataRepository, times(1)).findAll();
    }
}
