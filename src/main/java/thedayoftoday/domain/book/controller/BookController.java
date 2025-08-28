package thedayoftoday.domain.book.controller;

import thedayoftoday.domain.book.entity.Book;
import thedayoftoday.domain.book.dto.RecommendedBookResponseDto;
import thedayoftoday.domain.book.service.BookService;
import thedayoftoday.domain.diary.entity.Diary;
import thedayoftoday.domain.user.entity.User;
import thedayoftoday.domain.diary.repository.DiaryRepository;
import thedayoftoday.domain.user.repository.UserRepository;
import thedayoftoday.domain.auth.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/book")
public class BookController {

    private final BookService bookService;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    @PostMapping("/recommend")
    public ResponseEntity<Void> recommendBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("diaryId") Long diaryId
    ) {
        bookService.recommendBookToDiary(diaryId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/show")
    public ResponseEntity<RecommendedBookResponseDto> getRecommendedBook(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RecommendedBookResponseDto responseDto = bookService.getRecommendedBookForUser(userDetails.getUserId());
        return ResponseEntity.ok(responseDto);
    }
}
