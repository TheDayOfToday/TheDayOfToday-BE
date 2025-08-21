package thedayoftoday.domain.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import thedayoftoday.domain.diary.conversation.service.ConversationService;
import thedayoftoday.domain.diary.dto.*;
import thedayoftoday.domain.diary.entity.Diary;
import thedayoftoday.domain.diary.exception.DiaryAccessDeniedException;
import thedayoftoday.domain.diary.exception.DiaryNotFoundException;
import thedayoftoday.domain.diary.moodmeter.DiaryMood;
import thedayoftoday.domain.diary.moodmeter.MoodCategoryResponse;
import thedayoftoday.domain.diary.moodmeter.MoodMeter;
import thedayoftoday.domain.diary.moodmeter.RecommendedMoodResponseDto;
import thedayoftoday.domain.diary.repository.DiaryRepository;
import thedayoftoday.domain.user.entity.User;
import thedayoftoday.domain.user.service.UserService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserService userService;
    private final AiService aiService;
    private final ConversationService conversationService;

    @Transactional
    public DiaryIdResponseDto createDiaryFromAudio(Long userId, MultipartFile audioFile) throws IOException {
        String transcribedText = aiService.transcribeAudio(audioFile);
        DiaryContentResponseDto diaryDto = aiService.convertToDiary(transcribedText);
        DiaryMood mood = aiService.recommendMood(diaryDto.content());
        return createDiary(userId, diaryDto.title(), diaryDto.content(), mood);
    }

    @Transactional
    public void completeDiaryFromConversation(Long userId, Long diaryId, String lastQuestion, MultipartFile lastAudioFile) throws IOException {
        Diary diaryWithConversations = diaryRepository.findByIdWithConversations(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException(diaryId));

        if (!Objects.equals(diaryWithConversations.getUser().getUserId(), userId)) {
            throw new DiaryAccessDeniedException();
        }

        if (lastAudioFile != null && !lastAudioFile.isEmpty()) {
            String lastAnswer = aiService.transcribeAudio(lastAudioFile);
            conversationService.save(lastQuestion, lastAnswer, diaryWithConversations);
        }

        String mergedText = conversationService.mergeConversationText(diaryWithConversations);
        DiaryContentResponseDto diaryDto = aiService.convertToDiary(mergedText);
        diaryWithConversations.updateDiary(diaryDto.title(), diaryDto.content());
    }

    @Transactional
    public AIAnalysisContentDto analyzeAndSaveDiary(Long userId, Long diaryId) {
        Diary diary = getDiaryByIdAndAuthorize(diaryId, userId);
        DiaryMood mood = diary.getMoodForAnalysis();

        String content = diary.getContent();
        String userName = diary.getUser().getName();
        String analysisResult = aiService.analyzeDiaryContent(content, mood, Optional.of(userName));
        diary.upDateAnalysisContent(analysisResult);
        return new AIAnalysisContentDto(analysisResult);
    }

    @Transactional
    public void updateDiaryMood(Long userId, Long diaryId, DiaryMood mood) {
        Diary diary = getDiaryByIdAndAuthorize(diaryId, userId);
        diary.updateDiaryMood(mood);
    }

    @Transactional
    public DiaryIdResponseDto createDiary(Long userId, String title, String content, DiaryMood mood) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Diary newDiary = Diary.createNewDiary(user, title, content, mood);
        diaryRepository.save(newDiary);
        return new DiaryIdResponseDto(newDiary.getDiaryId());
    }

    @Transactional
    public void updateDiaryContent(Long userId, Long diaryId, String title, String content) {
        Diary diary = getDiaryByIdAndAuthorize(diaryId, userId);
        diary.updateDiary(title, content);
    }

    @Transactional
    public DiaryIdResponseDto createEmptyDiary(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Diary newDiary = Diary.createEmptyDiary(user);
        diaryRepository.save(newDiary);
        return new DiaryIdResponseDto(newDiary.getDiaryId());
    }

    public DiaryInfoResponseDto findDiary(Long userId, Long diaryId) {
        Diary diary = getDiaryByIdAndAuthorize(diaryId, userId);

        return new DiaryInfoResponseDto(
                diary.getUser().getName(),
                diary.getTitle(),
                diary.getContent(),
                diary.getCreateTime(),
                diary.getMoodOrDefault(),
                diary.getAnalysisContentOrDefault()
        );
    }

    @Transactional
    public void deleteDiaryByDate(Long userId, int year, int month, int day) {
        LocalDate date = LocalDate.of(year, month, day);
        diaryRepository.deleteByUser_UserIdAndCreateTimeBetween(userId, date, date);
    }

    public DiaryContentResponseDto getDiaryContent(Long userId, Long diaryId) {
        Diary diary = getDiaryByIdAndAuthorize(diaryId, userId);
        return new DiaryContentResponseDto(diary.getTitle(), diary.getContent());
    }

    public RecommendedMoodResponseDto getRecommendedMood(Long userId, Long diaryId) {
        Diary diary = getDiaryByIdAndAuthorize(diaryId, userId);
        DiaryMood mood = aiService.recommendMood(diary.getContent());

        List<MoodCategoryResponse> moodCategories = MoodMeter.getAllMoodCategories();

        return new RecommendedMoodResponseDto(mood, moodCategories);
    }

    private Diary getDiaryByIdAndAuthorize(Long diaryId, Long userId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException("해당 일기를 찾을 수 없습니다. ID: " + diaryId));

        if (!Objects.equals(diary.getUser().getUserId(), userId)) {
            throw new DiaryAccessDeniedException();
        }
        return diary;
    }

    public Diary findAuthorizedDiaryByIdWithConversations(Long diaryId, Long userId) {
        Diary diary = diaryRepository.findByIdWithConversations(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException(diaryId));

        if (!Objects.equals(diary.getUser().getUserId(), userId)) {
            throw new DiaryAccessDeniedException();
        }
        return diary;
    }

    public List<Diary> findDiariesByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findByUser_UserIdAndCreateTimeBetween(userId, startDate, endDate);
    }

    public Optional<Diary> findDiaryByDate(Long userId, LocalDate date) {
        return findDiariesByUserAndDateRange(userId, date, date).stream().findFirst();
    }

    public List<DailyMoodColorDto> findMoodColorsByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findMoodColorsByUserAndDateRange(userId, startDate, endDate);
    }
}
