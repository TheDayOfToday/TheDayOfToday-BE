package com.example.thedayoftoday.domain.entity.enumType;

import lombok.Getter;

@Getter
public enum Degree {

    NEUTRAL("중립"),
    POSITIVE("긍정"),
    NEGATIVE("부정");

    private final String degreeName;

    Degree(String degreeName) {
        this.degreeName = degreeName;
    }

}
