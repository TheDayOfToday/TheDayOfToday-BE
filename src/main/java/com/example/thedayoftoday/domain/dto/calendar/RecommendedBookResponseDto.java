package com.example.thedayoftoday.domain.dto.calendar;

import com.example.thedayoftoday.domain.entity.Book;

public record RecommendedBookResponseDto(
        String title,
        String author,
        String description,
        String coverImageUrl
) {
    public static RecommendedBookResponseDto from(Book book) {
        return new RecommendedBookResponseDto(
                book.getTitle(),
                book.getAuthor(),
                book.getDescription(),
                book.getCoverImageUrl()
        );
    }
}
