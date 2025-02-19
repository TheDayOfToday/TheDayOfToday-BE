package com.example.thedayoftoday.domain.entity;


import com.example.thedayoftoday.domain.entity.enumType.Moodmeter;
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

    private Moodmeter moodmeter;

    private String content;

    @OneToOne(mappedBy = "sentimentAnalysis")
    private Diary diary;


    @Builder
    public SentimentalAnalysis(String moodName,
                               Moodmeter moodmeter,
                               String content,
                               Diary diary) {
        this.moodName = moodName;
        this.moodmeter = moodmeter;
        this.diary = diary;
        this.content = content;
    }
}
