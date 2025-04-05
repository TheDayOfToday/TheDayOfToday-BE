package com.example.thedayoftoday.domain.dto.sentimentalAnalysis;

import java.util.Map;

public record MonthColorsResponseDto(
        Long userId,
        Map<String, String> colors
) {
}
