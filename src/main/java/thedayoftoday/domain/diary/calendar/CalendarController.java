package thedayoftoday.domain.diary.calendar;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import thedayoftoday.domain.diary.dto.AIAnalysisContentDto;
import thedayoftoday.domain.auth.security.CustomUserDetails;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import thedayoftoday.domain.diary.dto.DiaryContentResponseDto;

@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/{year}/{month}")
    public MonthColorsResponseDto pastGetMonthColors(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @PathVariable int year,
                                                 @PathVariable int month) {
        Long userId = userDetails.getUserId();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1); // 이번 달 말일
        return calendarService.getMonthColors(userId, startDate, endDate);
    }

    @GetMapping("/{yearMonth}")
    public MonthColorsResponseDto getMonthColors(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        Long userId = userDetails.getUserId();
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return calendarService.getMonthColors(userId, startDate, endDate);
    }

    @GetMapping("/diary/{year}/{month}/{day}")
    public DiaryContentResponseDto pastGetDiaryEntry(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @PathVariable int year,
                                                 @PathVariable int month,
                                                 @PathVariable int day) {
        Long userId = userDetails.getUserId();
        LocalDate date = LocalDate.of(year, month, day);
        return calendarService.pastGetDiaryEntry(userId, date);
    }

    @GetMapping("/{date}/diary")
    public DiaryContentResponseDto getDiaryEntry(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = userDetails.getUserId();
        return calendarService.getDiaryEntry(userId, date);
    }

    @GetMapping("/analysis/{year}/{month}/{day}")
    public AIAnalysisContentDto pastGetSentimentalAnalysis(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int year,
            @PathVariable int month,
            @PathVariable int day) {
        Long userId = userDetails.getUserId();
        LocalDate date = LocalDate.of(year, month, day);
        return calendarService.getSentimentalAnalysis(userId, date);
    }

    @GetMapping("/{date}/analysis")
    public AIAnalysisContentDto getSentimentalAnalysis(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = userDetails.getUserId();
        return calendarService.getSentimentalAnalysis(userId, date);
    }
}
