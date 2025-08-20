package thedayoftoday.domain.weeklyData.controller;

import thedayoftoday.domain.weeklyData.service.WeeklyAnalysisService;
import thedayoftoday.domain.weeklyData.dto.WeeklyAnalysisResponseDto;
import thedayoftoday.domain.auth.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weeklyAnalysis")
public class WeeklyAnalysisController {
    private final WeeklyAnalysisService weeklyAnalysisService;

    public WeeklyAnalysisController(WeeklyAnalysisService weeklyAnalysisService) {
        this.weeklyAnalysisService = weeklyAnalysisService;
    }

    @GetMapping("/{year}/{month}/{day}")
    public ResponseEntity<WeeklyAnalysisResponseDto> getWeeklyAnalysis(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int year,
            @PathVariable int month,
            @PathVariable int day) {
        Long userId = userDetails.getUserId();
        WeeklyAnalysisResponseDto weeklyAnalysisResponseDto = weeklyAnalysisService.getWeeklyAnalysis(userId, year, month, day);

        return ResponseEntity.ok(weeklyAnalysisResponseDto);
    }
}
