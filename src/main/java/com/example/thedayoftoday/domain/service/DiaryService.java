package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.DiaryCreateRequestDto;
import com.example.thedayoftoday.domain.dto.DiaryInfoResponseDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.DiaryMood;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import com.example.thedayoftoday.domain.repository.UserRepository;
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
    public void updateDiaryMood(Long diaryId, DiaryMood mood) {
        Diary diary = diaryRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리를 찾을 수 없습니다."));

        diary.updateDiaryMood(mood);
    }

    public DiaryCreateRequestDto createDiary(DiaryCreateRequestDto diaryCreateDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Diary newDiary = Diary.builder()
                .title(diaryCreateDto.title())
                .content(diaryCreateDto.content())
                .createTime(LocalDateTime.now())
                .diaryMood(diaryCreateDto.diaryMood())
                .user(user)
                .build();

        diaryRepository.save(newDiary);

        return new DiaryCreateRequestDto(newDiary.getTitle(), newDiary.getContent(), newDiary.getDiaryMood());
    }

    public void deleteDiary(Long diaryId) {
        Diary diary = diaryRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리가 존재하지 않습니다."));
        diaryRepository.delete(diary);
    }

    public DiaryCreateRequestDto createEmptyDiary(Long userId) {
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

        return new DiaryCreateRequestDto(newDiary.getTitle(), newDiary.getContent(), newDiary.getDiaryMood());
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
}
