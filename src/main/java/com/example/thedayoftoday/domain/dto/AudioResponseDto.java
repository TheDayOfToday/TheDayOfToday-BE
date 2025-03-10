package com.example.thedayoftoday.domain.dto;

public record AudioResponseDto(
        boolean success,
        String message
) {
    public static AudioResponseDto success(String message) {
        return new AudioResponseDto(true, message);
    }

    public static AudioResponseDto failure(String message) {
        return new AudioResponseDto(false, message);
    }
}
