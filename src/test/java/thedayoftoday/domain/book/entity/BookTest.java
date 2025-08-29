package thedayoftoday.domain.book.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookTest {

    @Test
    @DisplayName("of 정적 팩토리 메소드로 Book 객체를 성공적으로 생성한다")
    void createBookWithOfMethod() {
        // given
        String title = "테스트 책 제목";
        String author = "테스트 저자";
        String description = "테스트 책 설명입니다. 흥미로운 내용이 담겨 있습니다.";
        String coverImageUrl = "http://example.com/cover.jpg";

        // when
        Book book = Book.of(title, author, description, coverImageUrl);

        // then
        assertThat(book).isNotNull();
        assertThat(book.getTitle()).isEqualTo(title);
        assertThat(book.getAuthor()).isEqualTo(author);
        assertThat(book.getDescription()).isEqualTo(description);
        assertThat(book.getCoverImageUrl()).isEqualTo(coverImageUrl);
    }
}