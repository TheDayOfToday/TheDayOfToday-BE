package com.example.thedayoftoday.domain.entity.enumType;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum MoodMeter {

    HAPPY(Degree.POSITIVE, "행복한", "#FFD700"),
    EXCITED(Degree.POSITIVE, "신나는", "#FFA500"),
    CONFIDENT(Degree.POSITIVE, "자신감 있는", "#00FF00"),
    GRATEFUL(Degree.POSITIVE, "감사한", "#FFC0CB"),
    HOPEFUL(Degree.POSITIVE, "희망찬", "#E6E6FA"),
    INSPIRED(Degree.POSITIVE, "영감을 받은", "#FF69B4"),
    LOVING(Degree.POSITIVE, "사랑하는", "#FF6347"),
    DETERMINED(Degree.POSITIVE, "단호한", "#008080"),
    ENTHUSIASTIC(Degree.POSITIVE, "열정적인", "#FF4500"),

    NORMAL(Degree.NEUTRAL, "평범한", "#808080"),
    CALM(Degree.NEUTRAL, "차분한", "#A9A9A9"),
    CONTENT(Degree.NEUTRAL, "만족한", "#D3D3D3"),
    FOCUSED(Degree.NEUTRAL, "집중한", "#4682B4"),
    SHY(Degree.NEUTRAL, "수줍은", "#B0C4DE"),
    CURIOUS(Degree.NEUTRAL, "호기심 많은", "#ADD8E6"),
    RELIEVED(Degree.NEUTRAL, "안도한", "#F5F5DC"),

    SAD(Degree.NEGATIVE, "슬픈", "#000080"),
    ANGRY(Degree.NEGATIVE, "화난", "#8B0000"),
    LONELY(Degree.NEGATIVE, "외로운", "#4B0082"),
    DISAPPOINTED(Degree.NEGATIVE, "실망한", "#696969"),
    GUILTY(Degree.NEGATIVE, "죄책감이 드는", "#DC143C"),
    FRUSTRATED(Degree.NEGATIVE, "좌절한", "#A52A2A"),
    TIRED(Degree.NEGATIVE, "피곤한", "#708090"),
    STRESSED(Degree.NEGATIVE, "스트레스받은", "#2F4F4F"),
    AFRAID(Degree.NEGATIVE, "두려운", "#800000"),
    ASHAMED(Degree.NEGATIVE, "부끄러운", "#FF0000"),
    OVERWHELMED(Degree.NEGATIVE, "압도된", "#8B4513"),
    JEALOUS(Degree.NEGATIVE, "질투하는", "#556B2F"),
    HOPELESS(Degree.NEGATIVE, "절망적인", "#8B0000"),
    MELANCHOLIC(Degree.NEGATIVE, "우울한", "#483D8B"),
    REGRETFUL(Degree.NEGATIVE, "후회하는", "#5F9EA0"),
    BITTER(Degree.NEGATIVE, "씁쓸한", "#696969");

    private final Degree degree;
    private final String moodName;
    private final String color;

    MoodMeter(Degree degree, String moodName, String color) {
        this.degree = degree;
        this.moodName = moodName;
        this.color = color;
    }

    public static MoodMeter fromMoodName(String moodName) {
        return Arrays.stream(values())
                .filter(mood -> mood.moodName.equals(moodName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 감정을 찾을 수 없습니다: " + moodName));
    }
    public static MoodMeter fromColor(String color) {
        return Arrays.stream(values())
                .filter(mood -> mood.color.equals(color))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 색상을 찾을 수 없습니다: " + color));
    }
}