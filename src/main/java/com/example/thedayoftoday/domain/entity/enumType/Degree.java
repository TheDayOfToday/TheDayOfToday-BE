package com.example.thedayoftoday.domain.entity.enumType;

import lombok.Getter;

@Getter
public enum Degree {
    GOOD("좋은"),
    BAD("나쁜"),
    COMFORT("편안한"),
    HARD("힘든"),
    UNKNOWN(""),
    NONE("미분석");

    private final String degreeName;

    Degree(String degreeName) {
        this.degreeName = degreeName;
    }

}