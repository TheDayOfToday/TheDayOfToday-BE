package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.DiaryBasicResponseDto;
import com.example.thedayoftoday.domain.dto.DiaryCreateRequestDto;
import com.example.thedayoftoday.domain.dto.DiaryWithMoodResponseDto;
import com.example.thedayoftoday.domain.dto.conversation.ConversationResponseDto;
import com.example.thedayoftoday.domain.entity.DiaryMood;
import com.example.thedayoftoday.domain.service.AiService;

import java.io.IOException;

import com.example.thedayoftoday.domain.service.ConversationService;
import com.example.thedayoftoday.domain.service.DiaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/Diary")
public class DiaryController {

    private final AiService openAiService;
    private final ConversationService conversationService;
    private final DiaryService diaryService;

    public DiaryController(AiService openAiService, ConversationService conversationService, DiaryService diaryService) {
        this.openAiService = openAiService;
        this.conversationService = conversationService;
        this.diaryService = diaryService;
    }

    //독백모드 음성 파일을 받아서 텍스트로 변환
    @PostMapping("/transcribe")
    public DiaryBasicResponseDto transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException {
        String transAudio = openAiService.transcribeAudio(file);
        return openAiService.convertToDiary(transAudio);
    }

    //독백모드 버튼
    @PostMapping("/single-mode")
    public ResponseEntity<DiaryWithMoodResponseDto> createDiaryWithMood(@RequestParam("file") MultipartFile file) throws IOException {
        String transcribedText = openAiService.transcribeAudio(file);
        DiaryBasicResponseDto diary = openAiService.convertToDiary(transcribedText);
        String mood = openAiService.recommendMood(transcribedText);
        return ResponseEntity.ok(
                new DiaryWithMoodResponseDto(diary.title(), diary.content(), mood)
        );
    }

    //사용자가 감정 선택
    @PostMapping("/update-mood")
    public ResponseEntity<Void> updateDiaryMood(@RequestParam Long diaryId, @RequestBody DiaryMood mood) {
        diaryService.updateDiaryMood(diaryId, mood);
        return ResponseEntity.ok().build();
    }

    //사용자가 일기 수정
    @PutMapping("/update-diary")
    public ResponseEntity<Void> updateDiaryContent(@RequestParam Long diaryId,
                                                   @RequestBody DiaryCreateRequestDto requestDto) {
        diaryService.updateDiaryContent(diaryId, requestDto.title(), requestDto.content());
        return ResponseEntity.ok().build();
    }

    //사용자 무드미터, 일기 토대로 감정 분석
    @GetMapping("/analyze")
    public ResponseEntity<String> analyzeDiary(@RequestParam Long diaryId,
                                               @RequestParam String moodName,
                                               @RequestParam String moodColor) {
        String analysis = openAiService.analyzeDiary(diaryId, new DiaryMood(moodName, moodColor));
        return ResponseEntity.ok(analysis);
    }

    //대화모드 시작 버튼
    @GetMapping("/conversation-mode/{userId}")
    public ResponseEntity<DiaryCreateRequestDto> startConversation(@PathVariable("userId") Long userId) {
        DiaryCreateRequestDto diaryResponseDto = diaryService.createEmptyDiary(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryResponseDto);
    }

    //대화모드 다음 버튼 누르면 음성 파일을 받아서 텍스트로 바꾸고 Conversation에 저장하고 텍스트 분석한 걸 바탕으로 질문 생성해서 던져줌
    @PostMapping("/conversation-mode/next-question")
    public ConversationResponseDto continueConversation(
            @RequestParam("question") String question,
            @RequestParam("file") MultipartFile file,
            @RequestParam("diaryId") Long diaryId) throws IOException {

        String answer = openAiService.transcribeAudio(file);
        // Conversation 저장
        conversationService.save(question, answer, diaryId);
        // 텍스트 바탕으로 다음 질문 생성
        String nextQuestion = openAiService.generateNextQuestion(answer);

        return new ConversationResponseDto(nextQuestion);
    }

    //대화모드 끝
    @PostMapping("/conversation-mode/complete")
    public ResponseEntity<DiaryWithMoodResponseDto> completeDiary(@RequestParam("diaryId") Long diaryId) {
        String mergedText = conversationService.mergeConversationText(diaryId);
        DiaryBasicResponseDto diary = openAiService.convertToDiary(mergedText);
        String mood = openAiService.recommendMood(diary.content());
        diaryService.updateDiaryContent(diaryId, diary.title(), diary.content());
        return ResponseEntity.ok(new DiaryWithMoodResponseDto(diary.title(), diary.content(), mood));
    }


    //일기 삭제
    @DeleteMapping("/delete/{diaryId}")
    public ResponseEntity<String> deleteDiary(@PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.ok("삭제가 완료되었습니다.");
    }
}