package com.example.thedayoftoday.domain.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public record DiaryMood(String moodName, String moodColor) {
}
