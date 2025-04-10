package com.example.thedayoftoday.domain.dto.calendar;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public record MonthColorsResponseDto(
        Long userId,
        @Schema(description = "날짜별 감정 색상", example = "{\"2025-04-01\": \"#FF0000\", \"2025-04-02\": \"#00FF00\"}")
        Map<String, String> colors
) {
}
