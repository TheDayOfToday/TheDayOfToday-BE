package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.DiaryRequestDto;
import com.example.thedayoftoday.domain.dto.DiaryResponseDto;
import com.example.thedayoftoday.domain.dto.SentimentalAnalysisResponseDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserService userService;
    private final SentimentalAnalysisService sentimentalAnalysisService;

    public DiaryService(DiaryRepository diaryRepository, UserService userService,
                        SentimentalAnalysisService sentimentalAnalysisService) {
        this.diaryRepository = diaryRepository;
        this.userService = userService;
        this.sentimentalAnalysisService = sentimentalAnalysisService;
    }

    public DiaryResponseDto createDiary(DiaryRequestDto diaryCreateDto, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Diary addDiary = Diary.builder()
                .title(diaryCreateDto.title())
                .content(diaryCreateDto.content())
                .createTime(LocalDateTime.now())
                .user(user)
                .build();

        diaryRepository.save(addDiary);

        SentimentalAnalysisResponseDto analysisDto = sentimentalAnalysisService.processSentimentalAnalysis();
        sentimentalAnalysisService.addAnalysis(analysisDto, addDiary.getDiaryId());

        return toDiaryResponseDto(user, addDiary);
    }


    public void deleteDiary(Long diaryId) {
        Diary diary = diaryRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리가 존재하지 않습니다."));
        diaryRepository.delete(diary);
    }

    public DiaryResponseDto findDiary(Long diaryId) {
        Diary diary = diaryRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 다이어리가 존재하지 않습니다."));
        return toDiaryResponseDto(diary.getUser(), diary);
    }

    public List<DiaryResponseDto> findByTitle(Long userId, String title) {
        List<Diary> diaries = diaryRepository.findByUserIdAndTitleWithUser(userId, title);
        if (diaries.isEmpty()) {
            throw new IllegalArgumentException("일기가 없습니다.");
        }
        return diaries.stream()
                .map(diary -> toDiaryResponseDto(diary.getUser(), diary))
                .collect(Collectors.toList());
    }

    private DiaryResponseDto toDiaryResponseDto(User user, Diary diary) {
        return new DiaryResponseDto(
                user.getNickname(),
                diary.getTitle(),
                diary.getContent(),
                diary.getCreateTime(),
                diary.getSentimentAnalysis().getMoodName(),
                diary.getSentimentAnalysis().getMoodmeter(),
                diary.getSentimentAnalysis().getContent()
        );
    }
}
