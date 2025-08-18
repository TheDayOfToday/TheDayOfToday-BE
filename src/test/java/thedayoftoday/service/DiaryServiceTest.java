package thedayoftoday.service;

import thedayoftoday.dto.diary.DiaryIdResponseDto;
import thedayoftoday.dto.diary.moodmeter.MoodCategoryResponse;
import thedayoftoday.entity.Diary;
import thedayoftoday.entity.DiaryMood;
import thedayoftoday.entity.User;
import thedayoftoday.repository.DiaryRepository;
import thedayoftoday.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DiaryServiceTest {

    @InjectMocks
    private DiaryService diaryService;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createDiary_정상_생성() {
        Long userId = 1L;
        User mockUser = User.builder().userId(userId).name("광훈이바보").build();
        DiaryMood mood = new DiaryMood("행복", "#FF0000");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(diaryRepository.save(any(Diary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DiaryIdResponseDto result = diaryService.createDiary(userId, "제목", "내용", mood);

        assertThat(result).isNotNull();
        verify(diaryRepository).save(any(Diary.class));
    }

    @Test
    void createDiary_유저없음_예외() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                diaryService.createDiary(userId, "제목", "내용", new DiaryMood("기쁨", "#0000FF")));
    }

    @Test
    void deleteDiary_정상삭제() {
        Long userId = 1L;
        Long diaryId = 10L;
        User user = User.builder().userId(userId).build();
        Diary diary = Diary.builder().diaryId(diaryId).user(user).build();

        when(diaryRepository.findByDiaryId(diaryId)).thenReturn(Optional.of(diary));

        diaryService.deleteDiary(userId, diaryId);

        verify(diaryRepository).delete(diary);
    }

    @Test
    void updateDiaryContent_정상수정() {
        Long userId = 1L;
        Long diaryId = 10L;
        User user = User.builder().userId(userId).build();
        Diary diary = spy(Diary.builder().diaryId(diaryId).user(user).build());

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(diary));

        diaryService.updateDiaryContent(userId, diaryId, "새 제목", "새 내용");

        verify(diary).updateDiary("새 제목", "새 내용");
    }

    @Test
    void getAllMoodListResponseDto_감정_리스트_생성() {
        List<MoodCategoryResponse> result = diaryService.getAllMoodListResponseDto();
        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThan(0);
    }

    @Test
    void findDiaryByUserAndDiaryId_정상반환() {
        Long userId = 1L;
        Long diaryId = 10L;
        User user = User.builder().userId(userId).name("광훈이바보").build();
        Diary diary = Diary.builder().diaryId(diaryId).user(user).build();

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(diary));

        Diary found = diaryService.findDiaryByUserAndDiaryId(userId, diaryId);
        assertThat(found.getDiaryId()).isEqualTo(diaryId);
    }
}