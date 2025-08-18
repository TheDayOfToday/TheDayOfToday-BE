package thedayoftoday.domain;

import thedayoftoday.dto.calendar.MonthColorsResponseDto;
import thedayoftoday.dto.diary.AIAnalysisContentDto;
import thedayoftoday.dto.diary.DiaryBasicResponseDto;
import thedayoftoday.entity.Diary;
import thedayoftoday.entity.DiaryMood;
import thedayoftoday.repository.DiaryRepository;
import thedayoftoday.service.CalendarService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private DiaryRepository diaryRepository;

    @InjectMocks
    private CalendarService calendarService;

    private Diary nonEmptyDiary;
    private Diary emptyDiary;

    @BeforeEach
    void setUp() {
        DiaryMood diaryMood = new DiaryMood("기쁨", "#FFD700");

        nonEmptyDiary = Diary.builder()
                .title("제목")
                .content("내용")
                .createTime(LocalDate.of(2025, 5, 1))
                .diaryMood(diaryMood)
                .analysisContent("기쁨의 감정이 느껴집니다.")
                .build();

        emptyDiary = Diary.builder()
                .title(null)
                .content(null)
                .createTime(LocalDate.of(2025, 5, 1))
                .diaryMood(null)
                .analysisContent(null)
                .build();
    }

    @Test
    void getDiaryEntry_일기존재시반환() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2025, 5, 1);

        when(diaryRepository.findByUser_UserIdAndCreateTimeBetween(eq(userId), any(), any()))
                .thenReturn(List.of(nonEmptyDiary));

        DiaryBasicResponseDto result = calendarService.getDiaryEntry(userId, date);

        assertNotNull(result);
        assertEquals("제목", result.title());
        assertEquals("내용", result.content());
    }

    @Test
    void getDiaryEntry_모든일기비어있으면Null() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2025, 5, 1);

        when(diaryRepository.findByUser_UserIdAndCreateTimeBetween(eq(userId), any(), any()))
                .thenReturn(List.of(emptyDiary));

        DiaryBasicResponseDto result = calendarService.getDiaryEntry(userId, date);
        assertNull(result);
    }

    @Test
    void getSentimentalAnalysis_일기존재시분석내용반환() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2025, 5, 1);

        when(diaryRepository.findByUser_UserIdAndCreateTimeBetween(eq(userId), any(), any()))
                .thenReturn(List.of(nonEmptyDiary));

        AIAnalysisContentDto result = calendarService.getSentimentalAnalysis(userId, date);

        assertNotNull(result);
        assertEquals("기쁨의 감정이 느껴집니다.", result.analysis());
    }

    @Test
    void getSentimentalAnalysis_모든일기비어있으면기본분석반환() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2025, 5, 1);

        when(diaryRepository.findByUser_UserIdAndCreateTimeBetween(eq(userId), any(), any()))
                .thenReturn(List.of(emptyDiary));

        AIAnalysisContentDto result = calendarService.getSentimentalAnalysis(userId, date);

        assertNotNull(result);
        assertEquals("해당 날짜의 감정 분석 데이터가 없습니다.", result.analysis());
    }

    @Test
    void getMonthColors_감정분석된일기존재시_해당색상반환() {
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2025, 5, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 31);

        DiaryMood mood = new DiaryMood("기쁨", "#FFD700");
        Diary diary = Diary.builder()
                .createTime(LocalDate.of(2025, 5, 10))
                .diaryMood(mood)
                .analysisContent("기쁨의 감정이 느껴집니다.")
                .build();

        when(diaryRepository.findByUser_UserIdAndCreateTimeBetween(eq(userId), eq(startDate), eq(endDate)))
                .thenReturn(List.of(diary));

        MonthColorsResponseDto result = calendarService.getMonthColors(userId, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.colors().size());
        assertEquals("#FFD700", result.colors().get("2025-05-10"));
    }

    @Test
    void getMonthColors_감정미분석일기존재시_미분석문자반환() {
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2025, 5, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 31);

        Diary diary = Diary.builder()
                .createTime(LocalDate.of(2025, 5, 10))
                .diaryMood(null)
                .analysisContent(null)
                .build();

        when(diaryRepository.findByUser_UserIdAndCreateTimeBetween(eq(userId), eq(startDate), eq(endDate)))
                .thenReturn(List.of(diary));

        MonthColorsResponseDto result = calendarService.getMonthColors(userId, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.colors().size());
        assertEquals("미분석", result.colors().get("2025-05-10"));
    }
}
