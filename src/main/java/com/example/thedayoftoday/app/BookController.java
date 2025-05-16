package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.calendar.RecommendedBookResponseDto;
import com.example.thedayoftoday.domain.entity.Book;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import com.example.thedayoftoday.domain.repository.UserRepository;
import com.example.thedayoftoday.domain.security.CustomUserDetails;
import com.example.thedayoftoday.domain.service.BookService;
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
