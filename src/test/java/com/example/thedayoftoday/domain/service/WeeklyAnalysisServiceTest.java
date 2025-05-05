package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.weeklyAnalysis.WeeklyAnalysisResponseDto;
import com.example.thedayoftoday.domain.entity.WeeklyData;
import com.example.thedayoftoday.domain.repository.WeeklyDataRepository;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
                .title("1주차 감정 분석")
                .feedback("긍정긍정 맨~")
                .startDate(LocalDate.of(2025, 10, 27))
                .endDate(LocalDate.of(2025, 11, 2))
                .build();
    }

    @Test
    void 주어진_날짜에_해당하는_주간데이터가_있으면_정상적으로_반환된다() {
        // given
        Long userId = 1L;
        int year = 2025, month = 11, day = 1; // 11월 1일 → 주간: 10/27 ~ 11/2
        LocalDate date = LocalDate.of(year, month, day);

        LocalDate[] range = weeklyAnalysisService.calculateStartAndEndDate(date);
        LocalDate startDate = range[0];
        LocalDate endDate = range[1];

        when(weeklyDataRepository.findByUser_UserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                userId, endDate, startDate
        )).thenReturn(Optional.of(sampleData));

        // when
        WeeklyAnalysisResponseDto result = weeklyAnalysisService.getWeeklyAnalysis(userId, year, month, day);

        // then
        assertNotNull(result);
        assertEquals("1주차 감정 분석", result.title());
        assertEquals("긍정긍정 맨~", result.feedback());
        assertEquals(startDate, result.startDate());
        assertEquals(endDate, result.endDate());

        verify(weeklyDataRepository, times(1))
                .findByUser_UserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(userId, endDate, startDate);
    }

    @Test
    void 주어진_날짜에_해당하는_주간데이터가_없으면_null값으로_반환된다() {
        Long userId = 2L;
        int year = 2025;
        int month = 12;
        int day = 25;
        LocalDate date = LocalDate.of(year, month, day);

        LocalDate[] weekRange = weeklyAnalysisService.calculateStartAndEndDate(date);
        LocalDate startOfWeek = weekRange[0];
        LocalDate endOfWeek = weekRange[1];

        when(weeklyDataRepository.findByUser_UserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                userId, endOfWeek, startOfWeek
        )).thenReturn(Optional.empty());

        // when
        WeeklyAnalysisResponseDto result = weeklyAnalysisService.getWeeklyAnalysis(userId, year, month, day);

        // then
        assertNotNull(result);
        assertNull(result.title());
        assertNull(result.feedback());
        assertNull(result.degree());
        assertEquals(startOfWeek, result.startDate());
        assertEquals(endOfWeek, result.endDate());

        verify(weeklyDataRepository, times(1))
                .findByUser_UserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(userId, endOfWeek, startOfWeek);
    }
}