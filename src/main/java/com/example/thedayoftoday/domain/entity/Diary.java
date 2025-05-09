package com.example.thedayoftoday.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long diaryId;

    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private LocalDate createTime;

    @Embedded
    DiaryMood diaryMood;

    @Lob
    private String analysisContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_Id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<Conversation> conversations = new ArrayList<>();

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
}
