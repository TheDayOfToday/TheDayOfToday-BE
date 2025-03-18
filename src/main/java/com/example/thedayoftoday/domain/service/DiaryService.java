package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.DiaryAllRequestDto;
import com.example.thedayoftoday.domain.dto.DiaryAllResponseDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.DiaryMood;
import com.example.thedayoftoday.domain.entity.SentimentalAnalysis;
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

    public DiaryAllRequestDto createDiary(DiaryAllRequestDto diaryCreateDto, Long userId) {
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

        return new DiaryAllRequestDto(newDiary.getTitle(), newDiary.getContent(), newDiary.getDiaryMood());
    }

    public void deleteDiary(Long diaryId) {
        Diary diary = diaryRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리가 존재하지 않습니다."));
        diaryRepository.delete(diary);
    }

    public DiaryAllResponseDto findDiary(Long diaryId) {
        Diary diary = diaryRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 다이어리가 존재하지 않습니다."));

        SentimentalAnalysis sentimentalAnalysis = diary.getSentimentAnalysis();

        DiaryMood diaryMood = (diary.getDiaryMood()!= null) ? diary.getDiaryMood():new DiaryMood("저장된 감정 없음", "#FFFFFF");
        String analysisContent =
                (sentimentalAnalysis != null) ? sentimentalAnalysis.getAnalysisContent() : "감정 분석 결과가 없습니다.";

        return new DiaryAllResponseDto(
                diary.getUser().getName(),
                diary.getTitle(),
                diary.getContent(),
                diary.getCreateTime(),
                diary.getDiaryMood(),
                analysisContent
        );
    }
}
