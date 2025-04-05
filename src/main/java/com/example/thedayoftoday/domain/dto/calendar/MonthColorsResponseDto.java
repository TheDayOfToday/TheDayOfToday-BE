package com.example.thedayoftoday.domain.dto.calendar;

import java.util.Map;

public record MonthColorsResponseDto(
        Long userId,
        Map<String, String> colors
) {
}
