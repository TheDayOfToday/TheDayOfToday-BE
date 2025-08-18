package thedayoftoday.service;

import thedayoftoday.dto.diary.DiaryIdResponseDto;
import thedayoftoday.dto.diary.DiaryInfoResponseDto;
import thedayoftoday.dto.diary.moodmeter.MoodCategoryResponse;
import thedayoftoday.dto.diary.moodmeter.MoodDetailsDto;
import thedayoftoday.dto.diary.moodmeter.MoodMeterCategoryDto;
import thedayoftoday.entity.Diary;
import thedayoftoday.entity.DiaryMood;
import thedayoftoday.entity.User;
import thedayoftoday.entity.enumType.Degree;
import thedayoftoday.entity.enumType.MoodMeter;
import thedayoftoday.repository.DiaryRepository;
import thedayoftoday.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    public DiaryService(DiaryRepository diaryRepository, UserRepository userRepository) {
        this.diaryRepository = diaryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void updateDiaryMood(Long userId, Long diaryId, DiaryMood mood) {
        Diary diary = diaryRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리를 찾을 수 없습니다."));

        authorizeUser(userId, diary);

        diary.updateDiaryMood(mood);
    }

    @Transactional
    public DiaryIdResponseDto createDiary(Long userId, String title, String content, DiaryMood mood) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Diary newDiary = Diary.builder()
                .title(title)
                .content(content)
                .createTime(LocalDate.now())
                .diaryMood(mood)
                .user(user)
                .build();

        diaryRepository.save(newDiary);

        return new DiaryIdResponseDto(newDiary.getDiaryId());
    }

    @Transactional
    public void updateDiaryContent(Long userId, Long diaryId, String title, String content) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리가 존재하지 않습니다."));
        authorizeUser(userId, diary);
        diary.updateDiary(title, content);
    }

    @Transactional
    public void updateAnalysisContent(Long userId, Long diaryId, String content) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리가 존재하지 않습니다."));
        authorizeUser(userId, diary);
        diary.upDateAnalysisContent(content);
    }

    @Transactional
    public void deleteDiary(Long userId, Long diaryId) {
        Diary diary = diaryRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리가 존재하지 않습니다."));
        authorizeUser(userId, diary);
        diaryRepository.delete(diary);
    }

    @Transactional
    public DiaryIdResponseDto createEmptyDiary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Diary newDiary = Diary.builder()
                .title("작성중인 일기")
                .content("")
                .createTime(LocalDate.now())
                .diaryMood(null)
                .user(user)
                .build();

        diaryRepository.save(newDiary);

        return new DiaryIdResponseDto(newDiary.getDiaryId());
    }

    public DiaryMood getMoodByDiaryId(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new RuntimeException("Diary not found"));
        return new DiaryMood(diary.getDiaryMood().getMoodName(), diary.getDiaryMood().getMoodColor());
    }

    public DiaryInfoResponseDto findDiary(Long diaryId) {
        Diary diary = diaryRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 다이어리가 존재하지 않습니다."));

        DiaryMood diaryMood =
                (diary.getDiaryMood() != null) ? diary.getDiaryMood() : new DiaryMood("저장된 감정 없음", "#FFFFFF");
        String analysisContent = (diary.getAnalysisContent() != null) ? diary.getAnalysisContent() : "감정 분석 결과가 없습니다.";

        return new DiaryInfoResponseDto(
                diary.getUser().getName(),
                diary.getTitle(),
                diary.getContent(),
                diary.getCreateTime(),
                diaryMood,
                analysisContent
        );
    }

    public List<MoodCategoryResponse> getAllMoodListResponseDto() {
        Map<Degree, List<MoodDetailsDto>> moodGroup = new LinkedHashMap<>();

        for (Degree degree : Degree.values()) {
            moodGroup.put(degree, new ArrayList<>());
        }

        for (MoodMeter mood : MoodMeter.values()) {
            Degree degree = mood.getDegree();
            if (degree == null) {
                continue;
            }
            MoodDetailsDto dto = new MoodDetailsDto(mood.getMoodName(), mood.getColor());
            moodGroup.get(degree).add(dto);
        }

        List<MoodCategoryResponse> moodCategories = new ArrayList<>();

        for (Map.Entry<Degree, List<MoodDetailsDto>> entry : moodGroup.entrySet()) {
            moodCategories.add(
                    new MoodMeterCategoryDto(entry.getKey().getDegreeName(), entry.getValue())
            );
        }
        return moodCategories;
    }

    public Diary findDiaryByUserAndDiaryId(Long userId, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        authorizeUser(userId, diary);
        return diary;
    }

    private static void authorizeUser(Long userId, Diary diary) {
        if (!diary.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("권한 없음");
        }
    }
}
