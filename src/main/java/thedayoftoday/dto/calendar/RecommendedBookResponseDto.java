package thedayoftoday.dto.calendar;

import thedayoftoday.entity.Book;

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

    public static RecommendedBookResponseDto empty() {
        return new RecommendedBookResponseDto(null, null, null, null);
    }
}
