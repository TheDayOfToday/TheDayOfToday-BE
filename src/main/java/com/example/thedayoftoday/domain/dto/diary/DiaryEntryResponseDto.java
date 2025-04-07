package com.example.thedayoftoday.domain.dto.diary;

import java.util.List;

public record DiaryEntryResponseDto(
        String date,
        List<DiaryContentDto> entries
) {
}
