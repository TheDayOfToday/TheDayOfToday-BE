package thedayoftoday.domain.diary.calendar;

import lombok.RequiredArgsConstructor;
import thedayoftoday.domain.diary.dto.AIAnalysisContentDto;
import thedayoftoday.domain.diary.dto.DailyMoodColorDto;
import thedayoftoday.domain.diary.dto.DiaryContentResponseDto;
import thedayoftoday.domain.diary.entity.Diary;
import thedayoftoday.domain.diary.exception.DiaryNotFoundException;
import thedayoftoday.domain.diary.repository.DiaryRepository;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import thedayoftoday.domain.diary.service.DiaryService;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CalendarService {
    private final DiaryService diaryService;

    public MonthColorsResponseDto getMonthColors(Long userId, LocalDate startDate, LocalDate endDate) {
        List<DailyMoodColorDto> dailyMoods = diaryService.findMoodColorsByUserAndDateRange(userId, startDate, endDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, String> colors = dailyMoods.stream()
                .collect(Collectors.toMap(
                        dto -> dto.createTime().format(formatter),
                        DailyMoodColorDto::moodColor
                ));

        return new MonthColorsResponseDto(colors);
    }

    public DiaryContentResponseDto getDiaryEntry(Long userId, LocalDate date) {
        return diaryService.findDiaryByDate(userId, date)
                .map(diary -> new DiaryContentResponseDto(diary.getTitle(), diary.getContent()))
                .orElseThrow(() -> new DiaryNotFoundException("해당 날짜에 작성된 일기가 없습니다."));
    }

    public AIAnalysisContentDto getSentimentalAnalysis(Long userId, LocalDate date) {
        String analysisContent = diaryService.findDiaryByDate(userId, date)
                .map(Diary::getAnalysisContentOrDefault)
                .orElse("해당 날짜의 감정 분석 데이터가 없습니다.");

        return new AIAnalysisContentDto(analysisContent);
    }
}
