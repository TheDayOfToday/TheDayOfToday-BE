package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.DiaryRequestDto;
import com.example.thedayoftoday.domain.dto.DiaryResponseDto;
import com.example.thedayoftoday.domain.dto.SentimentalAnalysisResponseDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import com.example.thedayoftoday.domain.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserService userService;
    private final SentimentalAnalysisService sentimentalAnalysisService;

    public DiaryService(DiaryRepository diaryRepository, UserRepository userRepository, UserService userService,
                        SentimentalAnalysisService sentimentalAnalysisService) {
        this.diaryRepository = diaryRepository;
        this.userService = userService;
        this.sentimentalAnalysisService = sentimentalAnalysisService;
    }

    public Diary createDiary(DiaryRequestDto diaryCreateDto, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Diary addDiary = Diary.builder()
                .title(diaryCreateDto.getTitle())
                .content(diaryCreateDto.getContent())
                .createTime(LocalDateTime.now())
                .user(user)
                .build();
        diaryRepository.save(addDiary);

        SentimentalAnalysisResponseDto sentimentalAnalysisResponseDto = sentimentalAnalysisService.processSentimentalAnalysis();
        sentimentalAnalysisService.addAnalysis(sentimentalAnalysisResponseDto, addDiary.getDiaryId());

        return addDiary;
    }

    public void deleteDiary(Long diaryId) {
        Diary diary = diaryRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리가 존재하지 않습니다"));
        diaryRepository.delete(diary);
    }

    public Diary findDiary(Long diaryId) {
        return diaryRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 다이어리가 존재하지 않습니다."));
    }

    public DiaryResponseDto getDiaryResponseDto(Long userId, Diary updatedDiary) {

        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return DiaryResponseDto.builder()
                .nickName(user.getNickname())
                .diaryTitle(updatedDiary.getTitle())
                .diaryContent(updatedDiary.getContent())
                .createTime(LocalDateTime.now())
                .moodName(updatedDiary.getSentimentAnalysis().getMoodName())
                .moodMeter(updatedDiary.getSentimentAnalysis().getMoodmeter())
                .analysisContent(updatedDiary.getSentimentAnalysis().getContent())
                .build();
    }

    public List<DiaryResponseDto> findByTitle(Long userId, String title) {

        List<Diary> diaries = diaryRepository.findByUserIdAndTitleWithUser(userId, title);
        if (diaries.isEmpty()) {
            throw new IllegalArgumentException("일기가 없습니다.");
        }

        List<DiaryResponseDto> sameTitleDiaries = new ArrayList<>();
        for (Diary diary : diaries) {
            sameTitleDiaries.add(getDiaryResponseDto(diary.getUser().getUserId(), diary));
        }
        return sameTitleDiaries;
    }
}
