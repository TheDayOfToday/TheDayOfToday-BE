package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.MoodDetailsDto;
import com.example.thedayoftoday.domain.dto.MoodMeterCategoryDto;
import com.example.thedayoftoday.domain.dto.SentimentalAnalysisResponseDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.SentimentalAnalysis;
import com.example.thedayoftoday.domain.entity.enumType.Degree;
import com.example.thedayoftoday.domain.entity.enumType.MoodMeter;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import com.example.thedayoftoday.domain.repository.SentimentalAnalysisRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SentimentalAnalysisService {

    private final SentimentalAnalysisRepository sentimentalAnalysisRepository;
    private final DiaryRepository diaryRepository;

    public SentimentalAnalysisService(SentimentalAnalysisRepository sentimentalAnalysisRepository,
                                      DiaryRepository diaryRepository) {
        this.sentimentalAnalysisRepository = sentimentalAnalysisRepository;
        this.diaryRepository = diaryRepository;
    }

    public SentimentalAnalysisResponseDto processSentimentalAnalysis() {
        return SentimentalAnalysisResponseDto.defaultAnalysis();
    } // AI 분석 결과를 짜준다는 가정

    public void addAnalysis(SentimentalAnalysisResponseDto analysisDto, long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기가 존재하지 않습니다."));

        SentimentalAnalysis sentimentalAnalysis = new SentimentalAnalysis(
                analysisDto.moodName(),
                analysisDto.moodMeter(),
                analysisDto.content(),
                diary
        );

        sentimentalAnalysisRepository.save(sentimentalAnalysis);
        diary.addSentimentAnalysis(sentimentalAnalysis);
    }

    public List<MoodMeterCategoryDto> getAllMoodListResponseDto() {
        Map<Degree, List<MoodDetailsDto>> moodGroup = new LinkedHashMap<>();

        for (Degree value : Degree.values()) {
            moodGroup.put(value, new ArrayList<>());
        }

        for (MoodMeter mood : MoodMeter.values()) {
            moodGroup.get(mood.getDegree()).add(new MoodDetailsDto(mood.getMoodName(), mood.getColor()));
        }

        List<MoodMeterCategoryDto> moodCategories = new ArrayList<>();
        for (Map.Entry<Degree, List<MoodDetailsDto>> entry : moodGroup.entrySet()) {
            moodCategories.add(new MoodMeterCategoryDto(entry.getKey().getDegreeName(), entry.getValue()));
        }
        return moodCategories;
    }
}
