package thedayoftoday.domain.book.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import thedayoftoday.domain.book.dto.RecommendedBookResponseDto;
import thedayoftoday.domain.book.entity.Book;
import thedayoftoday.domain.diary.entity.Diary;
import thedayoftoday.domain.diary.service.DiaryService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private DiaryService diaryService;

    @Test
    @DisplayName("추천 책 조회 성공: 최신 일기(7일 이내)에 책이 추천된 경우")
    void getRecommendedBookForUser_Success() {
        // given
        Long userId = 1L;
        Book book = Book.of("테스트 책", "저자", "설명", "cover.jpg");
        Diary diary = Diary.builder().build();
        diary.setBook(book);

        ReflectionTestUtils.setField(diary, "createdAt", LocalDateTime.now().minusDays(1));

        given(diaryService.findMostRecentDiary(userId)).willReturn(Optional.of(diary));

        // when
        RecommendedBookResponseDto result = bookService.getRecommendedBookForUser(userId);

        // then
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.title()).isEqualTo(book.getTitle());
        assertThat(result.author()).isEqualTo(book.getAuthor());

        verify(diaryService).findMostRecentDiary(userId);
    }

    @Test
    @DisplayName("추천 책 조회 경계: 최신 일기가 정확히 7일 전이면 허용한다(정책이 그렇다면)")
    void getRecommendedBookForUser_Boundary_Exactly7Days() {
        // given
        Long userId = 1L;
        Book book = Book.of("테스트 책", "저자", "설명", "cover.jpg");
        Diary diary = Diary.builder().build();
        diary.setBook(book);
        ReflectionTestUtils.setField(diary, "createdAt", LocalDateTime.now().minusDays(7));

        given(diaryService.findMostRecentDiary(userId)).willReturn(Optional.of(diary));

        // when
        RecommendedBookResponseDto result = bookService.getRecommendedBookForUser(userId);

        // then (포함이라면)
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.title()).isEqualTo(book.getTitle());
    }


    @Test
    @DisplayName("추천 책 조회 실패: 최신 일기가 너무 오래된 경우(7일 초과)")
    void getRecommendedBookForUser_Fail_DiaryIsTooOld() {
        // given
        Long userId = 1L;
        Book book = Book.of("테스트 책", "저자", "설명", "cover.jpg");
        Diary diary = Diary.builder().build();
        diary.setBook(book);
        ReflectionTestUtils.setField(diary, "createdAt", LocalDateTime.now().minusDays(8));

        given(diaryService.findMostRecentDiary(userId)).willReturn(Optional.of(diary));

        // when
        RecommendedBookResponseDto result = bookService.getRecommendedBookForUser(userId);

        // then
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("추천 책 조회 실패: 사용자에게 작성된 일기가 없는 경우")
    void getRecommendedBookForUser_Fail_NoDiary() {
        // given
        Long userId = 1L;
        given(diaryService.findMostRecentDiary(userId)).willReturn(Optional.empty());

        // when
        RecommendedBookResponseDto result = bookService.getRecommendedBookForUser(userId);

        // then
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("추천 책 조회 실패: 최신 일기에 추천된 책이 없는 경우")
    void getRecommendedBookForUser_Fail_NoBookInDiary() {
        // given
        Long userId = 1L;
        Diary diary = Diary.builder().build();
        ReflectionTestUtils.setField(diary, "createdAt", LocalDateTime.now().minusDays(1));

        given(diaryService.findMostRecentDiary(userId)).willReturn(Optional.of(diary));

        // when
        RecommendedBookResponseDto result = bookService.getRecommendedBookForUser(userId);

        // then
        assertThat(result.isEmpty()).isTrue();
    }
}