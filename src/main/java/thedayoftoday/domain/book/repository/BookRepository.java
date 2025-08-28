package thedayoftoday.domain.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thedayoftoday.domain.book.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}