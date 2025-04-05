package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.weeklyAnalysis.WeeklyAnalysisResponseDto;
import com.example.thedayoftoday.domain.entity.WeeklyData;
import com.example.thedayoftoday.domain.repository.WeeklyDataRepository;
import java.time.temporal.WeekFields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
                .title("1주차 감정 분석")
                .feedback("긍정긍정 맨~")
                .startDate(LocalDate.of(2025, 10, 27))
                .endDate(LocalDate.of(2025, 11, 2))
                .build();
    }

    @Test
    void testGetWeeklyAnalysis_Week1_ShouldReturnCorrectData() {
        // given
        Long userId = 1L;
        int year = 2025;
        int month = 11;
        int week = 1;

        // 주차 범위 계산: 서비스 로직과 동일하게 계산
        LocalDate[] weekRange = calculateStartAndEndDate(year, month, week);
        LocalDate startOfWeek = weekRange[0];
        LocalDate endOfWeek = weekRange[1];

        when(weeklyDataRepository.findByUser_UserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                userId, endOfWeek, startOfWeek
        )).thenReturn(Optional.of(sampleData));

        // when
        WeeklyAnalysisResponseDto result = weeklyAnalysisService.getWeeklyAnalysis(userId, year, month, week);

        // then
        assertEquals(sampleData.getTitle(), result.title());
        assertEquals(sampleData.getFeedback(), result.feedback());
        assertEquals(sampleData.getStartDate(), result.startDate());
        assertEquals(sampleData.getEndDate(), result.endDate());

        verify(weeklyDataRepository, times(1))
                .findByUser_UserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(userId, endOfWeek, startOfWeek);
    }

    private LocalDate[] calculateStartAndEndDate(int year, int month, int week) {
        WeekFields weekFields = WeekFields.ISO;
        LocalDate baseDate = LocalDate.of(year, month, 1);
        LocalDate firstMonday = baseDate.with(weekFields.dayOfWeek(), 1);
        LocalDate startDate = firstMonday.plusWeeks(week - 1);
        LocalDate endDate = startDate.plusDays(6);
        return new LocalDate[]{startDate, endDate};
    }
}

//    @Test
//    void testGetWeeklyAnalysis_Success() {
//        when(weeklyDataRepository.findAll()).thenReturn(List.of(sampleData));
//
//        WeeklyAnalysisResponseDto result = weeklyAnalysisService.getWeeklyAnalysis(1L,2025, 11, 1);
//
//        assertEquals(2025, result.year());
//        assertEquals(11, result.month());
//        assertEquals(2, result.week());
//        assertEquals("2주차 감정 분석", result.title());
//        assertEquals("긍정긍정 맨~", result.feedback());
//        assertEquals(LocalDate.of(2025, 11, 3), result.startDate());
//        assertEquals(LocalDate.of(2025, 11, 9), result.endDate());
//
//        verify(weeklyDataRepository, times(1)).findAll();
//    }


//    @Test
//    void testGetWeeklyAnalysis_NoData() {
//        when(weeklyDataRepository.findAll()).thenReturn(List.of());
//
//        WeeklyAnalysisResponseDto result = weeklyAnalysisService.getWeeklyAnalysis(1L,2025, 11, 2);
//
//        assertEquals(2025, result.year());
//        assertEquals(11, result.month());
//        assertEquals(2, result.week());
//        assertNull(result.title());
//        assertNull(result.feedback());
//        assertNull(result.startDate());
//        assertNull(result.endDate());
//
//        verify(weeklyDataRepository, times(1)).findAll();
//    }

