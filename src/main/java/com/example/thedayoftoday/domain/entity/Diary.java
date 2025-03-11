package com.example.thedayoftoday.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true) // ✅ 기존 객체를 기반으로 새로운 객체 생성 가능
@AllArgsConstructor // ✅ Builder 사용 시 모든 필드 포함된 생성자 필요
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
                 User user,
                 SentimentalAnalysis sentimentAnalysis) { // ✅ 필드 추가
        this.title = title;
        this.content = content;
        this.createTime = createTime;
        this.user = user;
        this.sentimentAnalysis = sentimentAnalysis;
    }

    public void addSentimentAnalysis(SentimentalAnalysis sentimentAnalysis) {
        this.sentimentAnalysis = sentimentAnalysis;
    }
}
