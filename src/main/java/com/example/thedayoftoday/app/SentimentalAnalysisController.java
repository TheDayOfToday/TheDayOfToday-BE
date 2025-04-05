package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.sentimentalAnalysis.MoodCategoryResponse;
import com.example.thedayoftoday.domain.dto.sentimentalAnalysis.SentimentalAnalysisRequestDto;
import com.example.thedayoftoday.domain.dto.sentimentalAnalysis.SentimentalAnalysisResponseDto;
import com.example.thedayoftoday.domain.service.SentimentalAnalysisService;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sentimental")
public class SentimentalAnalysisController {

    private final SentimentalAnalysisService sentimentalAnalysisService;

    @GetMapping("/moodmeters")
    public List<MoodCategoryResponse> getMoodMeters() {
        return sentimentalAnalysisService.getAllMoodListResponseDto();
    }

    //텍스트 던져주면 AI가 만들어준 moodName, color, content 보여주기
    @PostMapping("/show")
    public SentimentalAnalysisResponseDto showSentimentalAnalysis(@RequestBody String text) {
        return sentimentalAnalysisService.processSentimentalAnalysis(text);
    }

    //AI가 만든 감정분석(사용자 관여 아예 없음) diary에 저장
    @PostMapping("/create/{diaryId}")
    public ResponseEntity<SentimentalAnalysisResponseDto> saveAnalysis(
            @RequestBody SentimentalAnalysisRequestDto sentimentalAnalysisRequestDto,
            @PathVariable Long diaryId) {
        SentimentalAnalysisResponseDto sentimentalAnalysisResponseDto = sentimentalAnalysisService.addAnalysis(
                sentimentalAnalysisRequestDto, diaryId);
        return ResponseEntity.ok(sentimentalAnalysisResponseDto);
    }
}
