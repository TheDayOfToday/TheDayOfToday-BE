package thedayoftoday.domain.book.entity;

import jakarta.persistence.*; // javax.persistence -> jakarta.persistence로 변경되었을 수 있습니다.
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import thedayoftoday.domain.common.BaseEntity;
import thedayoftoday.domain.diary.entity.Diary;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private String coverImageUrl;

    private Book(String title, String author, String description, String coverImageUrl) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
    }

    public static Book of(String title, String author, String description, String coverImageUrl) {
        return new Book(title, author, description, coverImageUrl);
    }

    @OneToOne(mappedBy = "book", fetch = FetchType.LAZY)
    @Setter
    private Diary diary;
}
