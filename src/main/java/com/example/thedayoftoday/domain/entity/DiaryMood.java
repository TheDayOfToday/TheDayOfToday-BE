package com.example.thedayoftoday.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
public class DiaryMood {
    @Column
    private String moodName;

    @Column
    private String moodColor;

    public DiaryMood(String moodName, String moodColor) {
        this.moodName = moodName;
        this.moodColor = moodColor;
    }

    public String getMoodName() {
        return moodName;
    }

    public String getMoodColor() {
        return moodColor;
    }
}
