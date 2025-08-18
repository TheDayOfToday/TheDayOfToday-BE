package thedayoftoday.service;

import thedayoftoday.dto.diary.AIAnalysisContentDto;
import thedayoftoday.dto.diary.DiaryBasicResponseDto;
import thedayoftoday.dto.calendar.MonthColorsResponseDto;
import thedayoftoday.entity.Diary;
import thedayoftoday.repository.DiaryRepository;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CalendarService {
    private final DiaryRepository diaryRepository;

    public CalendarService(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }

    public MonthColorsResponseDto getMonthColors(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Diary> diaries = diaryRepository.findByUser_UserIdAndCreateTimeBetween(userId, startDate, endDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, String> colors = new HashMap<>();

        for (Diary diary : diaries) {
            String dateKey = diary.getCreateTime().format(formatter);
            String moodColor = "미분석";

            if (diary.getDiaryMood() != null && !diary.isEmpty()) {
                moodColor = diary.getDiaryMood().getMoodColor();
            }

            colors.put(dateKey, moodColor);
        }

        return new MonthColorsResponseDto(colors);
    }

    public DiaryBasicResponseDto getDiaryEntry(Long userId, LocalDate date) {

        return diaryRepository.findByUser_UserIdAndCreateTimeBetween(userId, date, date).stream()
                .filter(diary -> !diary.isEmpty())
                .findFirst()
                .map(diary -> new DiaryBasicResponseDto(diary.getTitle(), diary.getContent()))
                .orElse(null);
    }

    public AIAnalysisContentDto getSentimentalAnalysis(Long userId, LocalDate date) {

        List<Diary> diaries = diaryRepository.findByUser_UserIdAndCreateTimeBetween(userId, date, date);
        String analysisContent = "해당 날짜의 감정 분석 데이터가 없습니다.";

        for (Diary diary : diaries) {
            if (!diary.isEmpty()) {
                analysisContent = diary.getAnalysisContent();
            }
        }
        return new AIAnalysisContentDto(analysisContent);
    }
}
