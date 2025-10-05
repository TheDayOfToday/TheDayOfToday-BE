package thedayoftoday.domain.diary.entity;

import thedayoftoday.domain.book.entity.Book;
import thedayoftoday.domain.diary.exception.MoodNotSelectedException;
import thedayoftoday.domain.diary.moodmeter.DiaryMood;
import thedayoftoday.domain.user.entity.RoleType;
import thedayoftoday.domain.user.entity.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiaryTest {

    private User user() {
        return User.builder()
                .name("홍길동")
                .email("gildong@example.com")
                .password("ENC")
                .phoneNumber("01012345678")
                .role(RoleType.USER)
                .build();
    }

    @Test
    @DisplayName("createNewDiary: 필드들 세팅 및 날짜 기본값")
    void createNewDiary_setsFields() {
        User u = user();
        DiaryMood mood = new DiaryMood("기쁨", "#FFFF00");

        Diary d = Diary.createNewDiary(u, "제목", "내용", mood);

        assertThat(d.getDiaryId()).isNull();
        assertThat(d.getTitle()).isEqualTo("제목");
        assertThat(d.getContent()).isEqualTo("내용");
        assertThat(d.getUser()).isSameAs(u);
        assertThat(d.getDiaryMood()).isEqualTo(mood);
        assertThat(d.getCreateTime()).isEqualTo(LocalDate.now());
        assertThat(d.getConversations()).isNotNull().isEmpty();
        assertThat(d.getBook()).isNull();
        assertThat(d.isEmpty()).isTrue(); // analysisContent가 null이므로
    }

    @Test
    @DisplayName("createEmptyDiary: 제목/내용/감정 기본값")
    void createEmptyDiary_defaults() {
        User u = user();

        Diary d = Diary.createEmptyDiary(u);

        assertThat(d.getTitle()).isEqualTo("작성중인 일기");
        assertThat(d.getContent()).isEmpty();
        assertThat(d.getDiaryMood()).isNull();
        assertThat(d.getUser()).isSameAs(u);
        assertThat(d.getCreateTime()).isEqualTo(LocalDate.now());
        assertThat(d.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("updateDiary: 제목/내용 변경")
    void updateDiary_updates() {
        Diary d = Diary.createNewDiary(user(), "A", "B", null);

        d.updateDiary("새 제목", "새 내용");

        assertThat(d.getTitle()).isEqualTo("새 제목");
        assertThat(d.getContent()).isEqualTo("새 내용");
    }

    @Test
    @DisplayName("updateDiaryMood: 감정 변경")
    void updateDiaryMood_updates() {
        Diary d = Diary.createEmptyDiary(user());
        DiaryMood mood = new DiaryMood("슬픔", "#0000FF");

        d.updateDiaryMood(mood);

        assertThat(d.getDiaryMood()).isEqualTo(mood);
    }

    @Test
    @DisplayName("addAnalysisContent / upDateAnalysisContent: 분석 내용 세팅 및 isEmpty 변화")
    void analysisContent_setters_and_isEmpty() {
        Diary d = Diary.createEmptyDiary(user());

        // 초기: null → empty
        assertThat(d.isEmpty()).isTrue();

        d.addAnalysisContent("  "); // 공백만 → empty
        assertThat(d.isEmpty()).isTrue();

        d.upDateAnalysisContent("분석 결과");
        assertThat(d.isEmpty()).isFalse();
        assertThat(d.getAnalysisContentOrDefault()).isEqualTo("분석 결과");
    }

    @Test
    @DisplayName("getMoodOrDefault: null이면 기본 Mood 반환, 있으면 그대로")
    void getMoodOrDefault_behaviour() {
        Diary d1 = Diary.createEmptyDiary(user());
        DiaryMood def = d1.getMoodOrDefault();

        assertThat(def.getMoodName()).isEqualTo("저장된 감정 없음");
        assertThat(def.getMoodColor()).isEqualTo("#FFFFFF");

        DiaryMood mood = new DiaryMood("분노", "#FF0000");
        Diary d2 = Diary.createNewDiary(user(), "t", "c", mood);

        assertThat(d2.getMoodOrDefault()).isEqualTo(mood);
    }

    @Test
    @DisplayName("getMoodForAnalysis: 감정 미선택이면 예외, 있으면 반환")
    void getMoodForAnalysis_requiresMood() {
        Diary d1 = Diary.createEmptyDiary(user());
        assertThatThrownBy(d1::getMoodForAnalysis)
                .isInstanceOf(MoodNotSelectedException.class);

        DiaryMood mood = new DiaryMood("평온", "#00FF00");
        Diary d2 = Diary.createNewDiary(user(), "t", "c", mood);
        assertThat(d2.getMoodForAnalysis()).isEqualTo(mood);
    }

    @Test
    @DisplayName("getCalendarMoodColor: 감정 있고 분석 내용 비어있지 않으면 색상, 아니면 '미분석'")
    void getCalendarMoodColor_rules() {
        DiaryMood mood = new DiaryMood("행복", "#FFFF00");

        Diary a = Diary.createNewDiary(user(), "t", "c", mood);
        a.upDateAnalysisContent("분석 결과 있음");
        assertThat(a.getCalendarMoodColor()).isEqualTo("#FFFF00");

        Diary b = Diary.createNewDiary(user(), "t", "c", mood); // 분석 없음
        assertThat(b.getCalendarMoodColor()).isEqualTo("미분석");

        Diary c = Diary.createEmptyDiary(user()); // 감정 없음
        c.upDateAnalysisContent("분석은 있으나 감정 선택 없음");
        assertThat(c.getCalendarMoodColor()).isEqualTo("미분석");
    }

    @Test
    @DisplayName("setBook: 양방향 연관관계 보장 (book.setDiary 호출)")
    void setBook_setsBothSides() {
        Diary d = Diary.createEmptyDiary(user());
        Book book = mock(Book.class);

        d.setBook(book);

        assertThat(d.getBook()).isSameAs(book);
        verify(book).setDiary(d);
    }
}
