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

    private final DiaryRepository diaryRepository;
    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String apiKey;
    private static final String GPT_URL = "https://api.openai.com/v1/chat/completions";

    public SentimentalAnalysisService(DiaryRepository diaryRepository, RestTemplate restTemplate) {
        this.diaryRepository = diaryRepository;
        this.restTemplate = restTemplate;
    }

    //텍스트에 맞춰 AI가 분석한 무드미터한글, 감정분석한글 만들어주는것
    public SentimentalAnalysisResponseDto processSentimentalAnalysis(String text) {
        String moodName = analyzeMoodMeter(text);
        String moodColor = getColorByMoodName(moodName);
        String contentAnalysis = analyzeContentEmotion(text);

        return new SentimentalAnalysisResponseDto(moodName, moodColor, contentAnalysis);
    }

    public SentimentalAnalysisResponseDto addAnalysis(SentimentalAnalysisRequestDto sentimentalAnalysisRequestDto, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일기가 없습니다"));

        DiaryMood diaryMood = new DiaryMood(sentimentalAnalysisRequestDto.analysisMoodName(), sentimentalAnalysisRequestDto.analysisMoodColor());
        diary = diary.toBuilder()
                .diaryMood(diaryMood)
                .analysisContent(sentimentalAnalysisRequestDto.analysisContent())
                .build();

        diaryRepository.save(diary);

        return new SentimentalAnalysisResponseDto(
                diaryMood.getMoodName(),
                diaryMood.getMoodColor(),
                diary.getAnalysisContent()
        );
    }

    //감정분석 반환
    public String analyzeContentEmotion(String text) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "Analyze the emotions of the given text."),
                Map.of("role", "user", "content", "다음 텍스트의 감정을 분석해줘: " + text)
        ));

        String response = callOpenAiApi(requestBody);
        return extractContent(response);
    }

    //무드미터반환(한글 3글자)
    public String analyzeMoodMeter(String text) {
        String allowedMoods = Arrays.stream(MoodMeter.values())
                .map(MoodMeter::getMoodName)
                .collect(Collectors.joining(", "));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content",
                        "Analyze the emotion in the given text and return only one word from the following list: "
                                + allowedMoods),
                Map.of("role", "user", "content", text)
        ));

        String response = callOpenAiApi(requestBody);
        return extractMoodName(response);
    }

    public static String getColorByMoodName(String moodName) {
        return fromMoodName(moodName).getColor();
    }

    private String callOpenAiApi(Map<String, Object> requestBody) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(GPT_URL, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }

    private String extractMoodName(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new IllegalArgumentException("OpenAI 응답을 파싱하는 중 오류 발생: " + e.getMessage());
        }
    }

    private String extractContent(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new IllegalArgumentException("OpenAI 응답 파싱 중 오류 발생: " + e.getMessage());
        }
    }

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
