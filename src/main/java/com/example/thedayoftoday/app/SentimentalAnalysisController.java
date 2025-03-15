package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.MoodMeterCategoryDto;
import com.example.thedayoftoday.domain.service.SentimentalAnalysisService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sentimental")
public class SentimentalAnalysisController {

    private final SentimentalAnalysisService sentimentalAnalysisService;

    @GetMapping("/moodmeters")
    public List<MoodMeterCategoryDto> getMoodMeters() {
        return sentimentalAnalysisService.getAllMoodListResponseDto();
    }


}
