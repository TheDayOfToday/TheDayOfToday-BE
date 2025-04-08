package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.diary.DiaryRequestDto;
import com.example.thedayoftoday.domain.dto.diary.DiaryInfoResponseDto;
import com.example.thedayoftoday.domain.dto.diary.moodmeter.MoodCategoryResponse;
import com.example.thedayoftoday.domain.dto.diary.moodmeter.MoodDetailsDto;
import com.example.thedayoftoday.domain.dto.diary.moodmeter.MoodMeterCategoryDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.DiaryMood;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.entity.enumType.Degree;
import com.example.thedayoftoday.domain.entity.enumType.MoodMeter;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import com.example.thedayoftoday.domain.repository.UserRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
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
    public void updateDiaryContent(Long userId, Long diaryId, String title, String content) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리가 존재하지 않습니다."));
        authorizeUser(userId, diary);
        diary.updateDiary(title, content);
    }

    public void deleteDiary(Long userId, Long diaryId) {
        Diary diary = diaryRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리가 존재하지 않습니다."));
        authorizeUser(userId, diary);
        diaryRepository.delete(diary);
    }

    public DiaryRequestDto createEmptyDiary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Diary newDiary = Diary.builder()
                .title("작성중인 일기")
                .content("")
                .createTime(LocalDateTime.now())
                .diaryMood(null)
                .user(user)
                .build();

        diaryRepository.save(newDiary);

        return new DiaryRequestDto(newDiary.getDiaryId(), newDiary.getTitle(), newDiary.getContent(), newDiary.getDiaryMood());
    }

    public DiaryMood getMoodByDiaryId(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new RuntimeException("Diary not found"));
        return new DiaryMood(diary.getDiaryMood().getMoodName(), diary.getDiaryMood().getMoodColor());
    }

    public DiaryInfoResponseDto findDiary(Long diaryId) {
        Diary diary = diaryRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 다이어리가 존재하지 않습니다."));

        DiaryMood diaryMood = (diary.getDiaryMood() != null) ? diary.getDiaryMood() : new DiaryMood("저장된 감정 없음", "#FFFFFF");
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

    private static void authorizeUser(Long userId, Diary diary) {
        if (!diary.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("권한 없음");
        }
    }

    public List<MoodCategoryResponse> getAllMoodListResponseDto() {
        Map<Degree, List<MoodDetailsDto>> moodGroup = new LinkedHashMap<>();

        for (Degree degree : Degree.values()) {
            if (degree == Degree.NONE) continue;
            moodGroup.put(degree, new ArrayList<>());
        }

        for (MoodMeter mood : MoodMeter.values()) {
            Degree degree = mood.getDegree();
            if (degree == null || degree == Degree.NONE) {
                continue;
            }
            MoodDetailsDto dto = new MoodDetailsDto(mood.getMoodName(),mood.getColor());
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
    /* 일단추가해둠
        public List<MoodCategoryResponse> getMoodMeters() {
        return sentimentalAnalysisService.getAllMoodListResponseDto();
    }
     */
}
