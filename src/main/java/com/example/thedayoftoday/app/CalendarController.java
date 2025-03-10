package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.service.CalendarService;
import com.example.thedayoftoday.domain.dto.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/calendar")
public class CalendarController {
    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/{userId}/{year}/{month}")
    public MonthColorsResponseDto getMonthColors(@PathVariable Long userId,
                                                 @PathVariable int year,
                                                 @PathVariable int month) {
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);
        return calendarService.getMonthColors(userId, startDate, endDate);
    }

    @GetMapping("/diary/{userId}/{year}/{month}/{day}")
    public DiaryEntryResponseDto getDiaryEntry(@PathVariable Long userId,
                                               @PathVariable int year,
                                               @PathVariable int month,
                                               @PathVariable int day) {
        LocalDateTime date = LocalDateTime.of(year, month, day, 0, 0);
        return calendarService.getDiaryEntry(userId, date);
    }

    @GetMapping("/analysis/{userId}/{year}/{month}/{day}")
    public SentimentalAnalysisResponseDto getSentimentalAnalysis(@PathVariable Long userId,
                                                                 @PathVariable int year,
                                                                 @PathVariable int month,
                                                                 @PathVariable int day) {
        LocalDateTime date = LocalDateTime.of(year, month, day, 0, 0);
        return calendarService.getSentimentalAnalysis(userId, date);
    }
}
