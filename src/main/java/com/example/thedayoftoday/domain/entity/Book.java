package com.example.thedayoftoday.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class Book {

    private String title;
    private String author;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private String coverImageUrl;

    public static Book of(String title, String author, String description, String coverImageUrl) {
        return new Book(title, author, description, coverImageUrl);
    }
}
