package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.security.CustomUserDetails;
import com.example.thedayoftoday.domain.service.CalendarService;
import com.example.thedayoftoday.domain.dto.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);
        return calendarService.getMonthColors(userId, startDate, endDate);
    }

    @GetMapping("/diary/{year}/{month}/{day}")
    public DiaryEntryResponseDto getDiaryEntry(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable int year,
                                               @PathVariable int month,
                                               @PathVariable int day) {
        Long userId = userDetails.getUserId();
        LocalDateTime date = LocalDateTime.of(year, month, day, 0, 0);
        return calendarService.getDiaryEntry(userId, date);
    }

    @GetMapping("/analysis/{year}/{month}/{day}")
    public SentimentalAnalysisListResponseDto getSentimentalAnalysis(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int year,
            @PathVariable int month,
            @PathVariable int day) {
        Long userId = userDetails.getUserId();
        LocalDateTime date = LocalDateTime.of(year, month, day, 0, 0);
        return calendarService.getSentimentalAnalysis(userId, date);
    }
}

