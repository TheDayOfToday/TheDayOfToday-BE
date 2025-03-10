package com.example.thedayoftoday.domain.dto;

import java.util.List;

public record DiaryEntryResponseDto(
        Long userId,
        String date,
        List<DiaryContentDto> entries
) {
}
