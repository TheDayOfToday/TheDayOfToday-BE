package com.example.thedayoftoday.domain.entity;

import com.example.thedayoftoday.domain.entity.enumType.MoodMeter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    private String moodName;

    private MoodMeter moodmeter;

    private String content;

    @OneToOne(mappedBy = "sentimentAnalysis", orphanRemoval = true)
    private Diary diary;

    @Builder
    public SentimentalAnalysis(String moodName,
                               MoodMeter moodmeter,
                               String content,
                               Diary diary) {
        this.moodName = moodName;
        this.moodmeter = moodmeter;
        this.diary = diary;
        this.content = content;
    }
}
