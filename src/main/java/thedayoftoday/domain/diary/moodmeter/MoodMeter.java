package thedayoftoday.domain.diary.moodmeter;

import java.util.*;

import lombok.Getter;
import thedayoftoday.domain.weeklyData.entity.Degree;

@Getter
public enum MoodMeter {

    ANXIOUS(Degree.BAD, "불안한", "#73272b"),
    DISGUSTED(Degree.BAD, "불쾌한", "#772726"),
    SCARED(Degree.BAD, "겁먹은", "#ab332d"),
    WORRIED(Degree.BAD, "걱정하는", "#ab322e"),
    STRESSED(Degree.BAD, "스트레스 받는", "#cb5232"),
    ANGRY(Degree.BAD, "화난", "#d05233"),
    NERVOUS(Degree.BAD, "초조한", "#e46238"),
    IRRITATED(Degree.BAD, "짜증나는", "#e56239"),
    UNCOMFORTABLE(Degree.BAD, "마음이 불편한", "#e66239"),
    SHOCKED(Degree.BAD, "충격받은", "#e7813d"),
    NUMB(Degree.BAD, "망연자실한", "#e4833d"),
    RESTLESS(Degree.BAD, "안절부절못하는", "#e7813f"),

    SURPRISED(Degree.GOOD, "놀란", "#f5e87a"),
    SATISFIED(Degree.GOOD, "만족스러운", "#f5e978"),
    POSITIVE(Degree.GOOD, "긍정적인", "#f3d75c"),
    FOCUSED(Degree.GOOD, "집중하는", "#f5d75b"),
    GLAD(Degree.GOOD, "기쁜", "#f6d759"),
    JOYFUL(Degree.GOOD, "흥겨운", "#f3cd5a"),
    MOTIVATED(Degree.GOOD, "동기부여된", "#f2cd57"),
    EXCITED(Degree.GOOD, "흥분한", "#f2ce59"),
    HAPPY(Degree.GOOD, "행복한", "#f1cd59"),
    INSPIRED(Degree.GOOD, "영감을 받은", "#eebd55"),
    FUN(Degree.GOOD, "재미있는", "#edbd57"),
    THRILLED(Degree.GOOD, "짜릿한", "#edb149"),

    DISGUSTED2(Degree.HARD, "역겨운", "#201f77"),
    ISOLATED(Degree.HARD, "소외된", "#201f78"),
    DESPERATE(Degree.HARD, "절망한", "#1f1e77"),
    MISERABLE(Degree.HARD, "비참한", "#212291"),
    DEPRESSED(Degree.HARD, "우울한", "#1f2391"),
    DISCOURAGED(Degree.HARD, "낙담한", "#2c46b4"),
    LONELY(Degree.HARD, "쓸쓸한", "#2d47b5"),
    UNMOTIVATED(Degree.HARD, "의욕없는", "#3c71d8"),
    SAD(Degree.HARD, "슬픈", "#3c72d6"),
    BORED(Degree.HARD, "지루한", "#52a1ea"),
    TIRED(Degree.HARD, "피곤한", "#52a2e8"),
    EXHAUSTED(Degree.HARD, "지친", "#52a3e6"),

    PEACEFUL(Degree.COMFORT, "평온한", "#a1d34d"),
    RELAXED(Degree.COMFORT, "여유로운", "#a6d44e"),
    DROWSY(Degree.COMFORT, "나른한", "#a7d44b"),
    CALM(Degree.COMFORT, "차분한", "#8dbf44"),
    THOUGHTFUL(Degree.COMFORT, "생각에 잠긴", "#8cbe42"),
    PROUD(Degree.COMFORT, "흐뭇한", "#8cbf46"),
    COMFORTABLE(Degree.COMFORT, "편안한", "#79af40"),
    SERENE(Degree.COMFORT, "평화로운", "#79ae3d"),
    GRATEFUL(Degree.COMFORT, "감사하는", "#51a43b"),
    BLESSED(Degree.COMFORT, "축복받은", "#51a23c"),
    COZY(Degree.COMFORT, "안락한", "#4fa43b"),
    TOUCHED(Degree.COMFORT, "감동적인", "#468c33"),

    UNKNOWN(Degree.UNKNOWN, "모르겠는", "#eeeeee");

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
                .filter(mood -> mood.color.equalsIgnoreCase(color))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 색상을 찾을 수 없습니다: " + color));
    }

    public static List<MoodCategoryResponse> getAllMoodCategories() {
        Map<Degree, List<MoodDetailsDto>> moodGroup = new LinkedHashMap<>();
        for (Degree degree : Degree.values()) {
            moodGroup.put(degree, new ArrayList<>());
        }

        for (MoodMeter mood : MoodMeter.values()) {
            Degree degree = mood.getDegree();
            if (degree != null && moodGroup.containsKey(degree)) {
                MoodDetailsDto dto = new MoodDetailsDto(mood.getMoodName(), mood.getColor());
                moodGroup.get(degree).add(dto);
            }
        }

        List<MoodCategoryResponse> moodCategories = new ArrayList<>();
        for (Map.Entry<Degree, List<MoodDetailsDto>> entry : moodGroup.entrySet()) {
            moodCategories.add(
                    new MoodMeterCategoryDto(entry.getKey().getDegreeName(), entry.getValue())
            );
        }
        return moodCategories;
    }
}