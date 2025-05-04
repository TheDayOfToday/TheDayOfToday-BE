package com.example.thedayoftoday.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class Book {

    private String title;
    private String author;

    @Column(length = 2000)
    private String description;

    private String coverImageUrl;

    public static Book of(String title, String author, String description, String coverImageUrl) {
        return new Book(title, author, description, coverImageUrl);
    }
}
