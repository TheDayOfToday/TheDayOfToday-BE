package com.example.thedayoftoday.domain.entity.enumType;

public enum Degree {

    NEUTRAL("중립"),
    POSITIVE("긍정"),
    NEGATIVE("부정");

    private final String degreeName;

    Degree(String degreeName) {
        this.degreeName = degreeName;
    }

    public String getDegreeName() {
        return degreeName;
    }
}
