package thedayoftoday.domain.diary.moodmeter;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
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
}
