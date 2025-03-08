package com.example.thedayoftoday.domain.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryRequestDto {

    @NotNull
    private String title;

    @Lob
    @NotNull
    private String content;

}
