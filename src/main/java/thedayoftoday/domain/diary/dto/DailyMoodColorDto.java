package thedayoftoday.domain.diary.dto;

import java.time.LocalDate;

public record DailyMoodColorDto(
        LocalDate createTime,
        String moodColor
) {}
