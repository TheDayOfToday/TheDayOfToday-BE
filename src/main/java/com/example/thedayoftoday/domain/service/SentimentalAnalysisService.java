package com.example.thedayoftoday.domain.service;

import static com.example.thedayoftoday.domain.entity.enumType.MoodMeter.fromMoodName;

import com.example.thedayoftoday.domain.dto.diary.moodmeter.MoodCategoryResponse;
import com.example.thedayoftoday.domain.dto.diary.moodmeter.MoodDetailsDto;
import com.example.thedayoftoday.domain.dto.diary.moodmeter.MoodMeterCategoryDto;
import com.example.thedayoftoday.domain.dto.calendar.SentimentalAnalysisRequestDto;
import com.example.thedayoftoday.domain.dto.calendar.SentimentalAnalysisResponseDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.DiaryMood;
import com.example.thedayoftoday.domain.entity.enumType.Degree;
import com.example.thedayoftoday.domain.entity.enumType.MoodMeter;
import com.example.thedayoftoday.domain.repository.DiaryRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class SentimentalAnalysisService {

    public List<MoodCategoryResponse> getAllMoodListResponseDto() {
        Map<Degree, List<MoodDetailsDto>> moodGroup = new LinkedHashMap<>();

        for (Degree degree : Degree.values()) {
            // "미분석"은 건너뜀
            if (degree == Degree.NONE) continue;
            moodGroup.put(degree, new ArrayList<>());
        }

        for (MoodMeter mood : MoodMeter.values()) {
            Degree degree = mood.getDegree();
            if (degree == null || degree == Degree.NONE) {
                continue;
            }
            MoodDetailsDto dto = new MoodDetailsDto(mood.getMoodName(),mood.getColor());
            moodGroup.get(degree).add(dto);
        }

        List<MoodCategoryResponse> moodCategories = new ArrayList<>();

        for (Map.Entry<Degree, List<MoodDetailsDto>> entry : moodGroup.entrySet()) {
            moodCategories.add(
                    new MoodMeterCategoryDto(entry.getKey().getDegreeName(), entry.getValue())
            );
        }
        return moodCategories;
    }

}
