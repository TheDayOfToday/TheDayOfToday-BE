package com.example.thedayoftoday.app;


import com.example.thedayoftoday.domain.dto.WeeklyAnalysisResponseDto;
import com.example.thedayoftoday.domain.service.WeeklyAnalysisService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weeklyAnalysis")
public class WeeklyAnalysisController {
    private final WeeklyAnalysisService weeklyAnalysisService;

    public WeeklyAnalysisController(WeeklyAnalysisService weeklyAnalysisService) {
        this.weeklyAnalysisService = weeklyAnalysisService;
    }

    @GetMapping("/{year}/{month}/{week}")
    public WeeklyAnalysisResponseDto getWeeklyAnalysis(
            @PathVariable int year, @PathVariable int month, @PathVariable int week) {
        return weeklyAnalysisService.getWeeklyAnalysis(year, month, week);
    }
}
