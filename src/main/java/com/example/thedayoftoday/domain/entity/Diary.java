package com.example.thedayoftoday.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@AllArgsConstructor
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long diaryId;
    private String title;

    @Lob
    private String content;
    private LocalDateTime createTime;

    @Embedded
    DiaryMood diaryMood;

    @Lob
    private String analysisContent;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_Id", nullable = false)
    private User user;

    @Builder
    public Diary(String title,
                 String content,
                 LocalDateTime createTime,
                 DiaryMood diaryMood,
                 User user,
                 String analysisContent) {
        this.title = title;
        this.content = content;
        this.createTime = createTime;
        this.diaryMood = diaryMood;
        this.user = user;
        this.analysisContent = analysisContent;
    }

    public void addAnalysisContent(String analysisContent){
        this.analysisContent = analysisContent;
    }

//    public void addSentimentAnalysis(SentimentalAnalysis sentimentAnalysis) {
//        this.sentimentAnalysis = sentimentAnalysis;
//    }
}
