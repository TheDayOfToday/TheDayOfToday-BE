package thedayoftoday.domain.diary.calendar;

import thedayoftoday.domain.diary.dto.AIAnalysisContentDto;
import thedayoftoday.domain.auth.security.CustomUserDetails;

import java.time.LocalDate;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import thedayoftoday.domain.diary.dto.DiaryContentResponseDto;

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
    public DiaryContentResponseDto getDiaryEntry(@AuthenticationPrincipal CustomUserDetails userDetails,
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
