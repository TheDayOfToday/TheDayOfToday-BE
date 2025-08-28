package thedayoftoday.domain.diary.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.*;
import thedayoftoday.domain.book.entity.Book;
import thedayoftoday.domain.common.BaseEntity;
import thedayoftoday.domain.diary.exception.MoodNotSelectedException;
import thedayoftoday.domain.diary.moodmeter.DiaryMood;
import thedayoftoday.domain.user.entity.User;
import thedayoftoday.domain.diary.conversation.entity.Conversation;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class Diary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long diaryId;

    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private LocalDate createTime; // 곧 지울 예정

    @Embedded
    DiaryMood diaryMood;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String analysisContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_Id", nullable = false)
    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    @OneToMany(mappedBy = "diary", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<Conversation> conversations = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "book_id") // 외래 키(FK)를 지정
    private Book book;

    public void setBook(Book book) {
        this.book = book;
        if (book != null) {
            book.setDiary(this);
        }
    }

    public void addAnalysisContent(String analysisContent) {
        this.analysisContent = analysisContent;
    }

    public void updateDiary(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void updateDiaryMood(DiaryMood diaryMood) {
        this.diaryMood = diaryMood;
    }

    public void upDateAnalysisContent(String analysisContent) {
        this.analysisContent = analysisContent;
    }

    public Boolean isEmpty() {
        if (this.analysisContent == null) {
            return true;
        }
        return this.analysisContent.isBlank();
    }

    public static Diary createNewDiary(User user, String title, String content, DiaryMood mood) {
        return Diary.builder()
                .title(title)
                .content(content)
                .createTime(LocalDate.now())
                .diaryMood(mood)
                .user(user)
                .build();
    }

    public static Diary createEmptyDiary(User user) {
        return Diary.builder()
                .title("작성중인 일기")
                .content("")
                .createTime(LocalDate.now())
                .diaryMood(null)
                .user(user)
                .build();
    }

    public DiaryMood getMoodOrDefault() {
        return (this.diaryMood != null) ? this.diaryMood : new DiaryMood("저장된 감정 없음", "#FFFFFF");
    }

    public String getAnalysisContentOrDefault() {
        return (this.analysisContent != null) ? this.analysisContent : "감정 분석 결과가 없습니다.";
    }

    public DiaryMood getMoodForAnalysis() {
        if (this.diaryMood == null) {
            throw new MoodNotSelectedException();
        }
        return this.diaryMood;
    }

    public String getCalendarMoodColor() {
        if (this.diaryMood != null && !this.isEmpty()) {
            return this.diaryMood.getMoodColor();
        }
        return "미분석";
    }
}
