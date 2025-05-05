package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.diary.AIAnalysisContentDto;
import com.example.thedayoftoday.domain.dto.diary.DiaryBasicResponseDto;
import com.example.thedayoftoday.domain.dto.calendar.MonthColorsResponseDto;
import com.example.thedayoftoday.domain.security.CustomUserDetails;
import com.example.thedayoftoday.domain.service.CalendarService;
import java.time.LocalDate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/{year}/{month}")
    public MonthColorsResponseDto getMonthColors(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @PathVariable int year,
                                                 @PathVariable int month) {
        Long userId = userDetails.getUserId();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1); // 이번 달 말일
        return calendarService.getMonthColors(userId, startDate, endDate);
    }

    @GetMapping("/diary/{year}/{month}/{day}")
    public DiaryBasicResponseDto getDiaryEntry(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable int year,
                                               @PathVariable int month,
                                               @PathVariable int day) {
        Long userId = userDetails.getUserId();
        LocalDate date = LocalDate.of(year, month, day);
        return calendarService.getDiaryEntry(userId, date);
    }

    @GetMapping("/analysis/{year}/{month}/{day}")
    public AIAnalysisContentDto getSentimentalAnalysis(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int year,
            @PathVariable int month,
            @PathVariable int day) {
        Long userId = userDetails.getUserId();
        LocalDate date = LocalDate.of(year, month, day);
        return calendarService.getSentimentalAnalysis(userId, date);
    }
}

