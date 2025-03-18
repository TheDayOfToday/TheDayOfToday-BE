package com.example.thedayoftoday.domain.entity;

import com.example.thedayoftoday.domain.entity.enumType.MoodMeter;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SentimentalAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long analysisId;

    private String analysisMoodName;

    private String analysisMoodColor;

    @Lob
    private String analysisContent;

    @OneToOne(mappedBy = "sentimentAnalysis", orphanRemoval = true)
    private Diary diary;

    @Builder
    public SentimentalAnalysis(
            String analysisMoodName,
            String analysisMoodColor,
            String analysisContent,
            Diary diary) {
        this.analysisMoodName = analysisMoodName;
        this.analysisMoodColor = analysisMoodColor;
        this.diary = diary;
        this.analysisContent = analysisContent;
    }
}
