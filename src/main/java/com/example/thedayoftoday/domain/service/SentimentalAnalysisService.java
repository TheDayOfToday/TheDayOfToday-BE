package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.SentimentalAnalysisResponseDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.SentimentalAnalysis;
import com.example.thedayoftoday.domain.entity.enumType.MoodMeter;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import com.example.thedayoftoday.domain.repository.SentimentalAnalysisRepository;
import org.springframework.stereotype.Service;

@Service
public class SentimentalAnalysisService {

    private final SentimentalAnalysisRepository sentimentalAnalysisRepository;
    private final DiaryRepository diaryRepository;

    private SentimentalAnalysisService(SentimentalAnalysisRepository sentimentalAnalysisRepository,
                                       DiaryRepository diaryRepository) {
        this.sentimentalAnalysisRepository = sentimentalAnalysisRepository;
        this.diaryRepository = diaryRepository;
    }

    public SentimentalAnalysisResponseDto processSentimentalAnalysis() {
        return SentimentalAnalysisResponseDto.builder()
                .moodName("행복")
                .moodmeter(MoodMeter.HAPPY)
                .content("기분 좋은 하루")
                .build();
    } //임시 분석 코드. moodname, moodmeter, content 모두 AI가 짜준다는 가정하임.

    public void addAnalysis(SentimentalAnalysisResponseDto sentimentalAnalysisResponseDto,
                            long diaryId) {
        Diary diary = diaryRepository.findById(diaryId).
                orElseThrow(() -> new IllegalArgumentException("해당 일기가 존재하지 않습니다."));

        SentimentalAnalysis sentimentalAnalysis = SentimentalAnalysis.builder()
                .moodName(sentimentalAnalysisResponseDto.getMoodName())
                .moodmeter(sentimentalAnalysisResponseDto.getMoodmeter())
                .diary(diary)
                .content(sentimentalAnalysisResponseDto.getContent())
                .build();
        sentimentalAnalysisRepository.save(sentimentalAnalysis);
        diary.addSentimentAnalysis(sentimentalAnalysis);
    }
}
