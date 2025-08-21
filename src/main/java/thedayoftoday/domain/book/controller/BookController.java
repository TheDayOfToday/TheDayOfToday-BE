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
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        bookService.recommendBook(userDetails.getUserId(), diary.getContent(), diary.getAnalysisContent());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/show")
    public ResponseEntity<RecommendedBookResponseDto> getRecommendedBook(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Book book = user.getRecommendedBook();

        boolean hasRecentDiary = diaryRepository.existsByUserAndCreateTimeAfter(user, LocalDate.now().minusDays(7));

        if (book == null || !hasRecentDiary) {
            RecommendedBookResponseDto emptyResponse = RecommendedBookResponseDto.empty();
            return ResponseEntity.ok(emptyResponse);
        }

        return ResponseEntity.ok(RecommendedBookResponseDto.from(book));
    }
}
