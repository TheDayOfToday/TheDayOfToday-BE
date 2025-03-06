package com.example.thedayoftoday.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long diaryId;

    private String title;

    @Lob
    private String content;

    private LocalDateTime createTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_Id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "analysis_id_id")
    private SentimentalAnalysis sentimentAnalysis;

    @Builder
    public Diary(String title, String content,
                 LocalDateTime createTime,
                 User user) {
        this.title = title;
        this.content = content;
        this.createTime = createTime;
        this.user = user;
    }

    public void addSentimentAnalysis(SentimentalAnalysis sentimentAnalysis) {
        this.sentimentAnalysis = sentimentAnalysis;
    }
}