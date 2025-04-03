package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.WeeklyAnalysisResponseDto;
import com.example.thedayoftoday.domain.security.CustomUserDetails;
import com.example.thedayoftoday.domain.service.WeeklyAnalysisService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weeklyAnalysis")
public class WeeklyAnalysisController {
    private final WeeklyAnalysisService weeklyAnalysisService;

    public WeeklyAnalysisController(WeeklyAnalysisService weeklyAnalysisService) {
        this.weeklyAnalysisService = weeklyAnalysisService;
    }

    @GetMapping("/{year}/{month}/{week}")
    public WeeklyAnalysisResponseDto getWeeklyAnalysis(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @PathVariable int year, @PathVariable int month,
                                                       @PathVariable int week) {
        Long userId = userDetails.getUserId();
        return weeklyAnalysisService.getWeeklyAnalysis(userId, year, month, week);
    }
}
