package thedayoftoday.domain.diary.controller;

import lombok.RequiredArgsConstructor;
import thedayoftoday.domain.diary.dto.*;
import thedayoftoday.domain.diary.entity.Diary;
import thedayoftoday.domain.diary.moodmeter.DiaryMood;
import thedayoftoday.domain.diary.moodmeter.RecommendedMoodResponseDto;
import thedayoftoday.domain.diary.conversation.dto.ConversationResponseDto;
import thedayoftoday.domain.diary.service.DiaryService;
import thedayoftoday.domain.auth.security.CustomUserDetails;

import java.io.IOException;

import thedayoftoday.domain.diary.conversation.service.ConversationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final ConversationService conversationService;
    private final DiaryService diaryService;

    @PostMapping(value = "/monologue", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiaryIdResponseDto> createDiaryWithMood(@RequestParam("file") MultipartFile file,
                                                                  @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        DiaryIdResponseDto responseDto = diaryService.createDiaryFromAudio(userDetails.getUserId(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    //대화모드 시작 버튼
    @PostMapping("/conversation-mode/start")
    public ResponseEntity<DiaryIdResponseDto> startConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        DiaryIdResponseDto responseDto = diaryService.createEmptyDiary(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    //대화모드 다음 버튼 누르면 음성 파일을 받아서 텍스트로 바꾸고 Conversation에 저장하고 텍스트 분석한 걸 바탕으로 질문 생성해서 던져줌
    @PostMapping(value = "/conversation-mode/next-question", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ConversationResponseDto continueConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("question") String question,
            @RequestParam("file") MultipartFile file,
            @RequestParam("diaryId") Long diaryId) throws IOException {

        Diary diary = diaryService.findAuthorizedDiaryById(diaryId, userDetails.getUserId());

        return conversationService.proccessAndGenerateNextQuestion(question, file, diary);
    }

    //대화모드 끝
    @PostMapping("/conversation-mode/complete")
    public ResponseEntity<DiaryIdResponseDto> completeDiary(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                            @RequestParam("question") String question,
                                                            @RequestParam(value = "file", required = false) MultipartFile file,
                                                            @RequestParam("diaryId") Long diaryId) throws IOException {
        diaryService.completeDiaryFromConversation(userDetails.getUserId(), diaryId, question, file);
        return ResponseEntity.ok(new DiaryIdResponseDto(diaryId));
    }

    @GetMapping("/moodmeter")
    public RecommendedMoodResponseDto showDiaryMood(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                    @RequestParam(value = "diaryId") Long diaryId) {
        return diaryService.getRecommendedMood(userDetails.getUserId(), diaryId);
    }

    //사용자가 감정 선택
    @PostMapping("/moodmeter")
    public ResponseEntity<String> updateDiaryMood(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @RequestParam(value = "diaryId") Long diaryId,
                                                  @RequestBody DiaryMood mood) {
        Long userId = userDetails.getUserId();
        diaryService.updateDiaryMood(userId, diaryId, mood);
        return ResponseEntity.ok("감정이 성공적으로 저장되었습니다.");
    }

    //일기 조회
    @GetMapping("/show")
    public DiaryContentResponseDto showDiary(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestParam(value = "diaryId") Long diaryId) {
        return diaryService.getDiaryContent(userDetails.getUserId(), diaryId);
    }

    //사용자가 일기 수정
    @PutMapping("/update-diary")
    public ResponseEntity<String> updateDiaryContent(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @RequestBody UpdateDiaryContentRequestDto updateDiaryContentRequestDto) {
        Long userId = userDetails.getUserId();
        diaryService.updateDiaryContent(userId, updateDiaryContentRequestDto.diaryId(), updateDiaryContentRequestDto.title(), updateDiaryContentRequestDto.content());
        return ResponseEntity.ok("일기 수정 완료");
    }

    //사용자 무드미터, 일기 토대로 감정 분석
    @PostMapping("/analyze")
    public ResponseEntity<AIAnalysisContentDto> analyzeDiary(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                             @RequestParam(value = "diaryId") Long diaryId) {
        AIAnalysisContentDto responseDto = diaryService.analyzeAndSaveDiary(userDetails.getUserId(), diaryId);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/delete/{year}/{month}/{day}")
    public ResponseEntity<Void> deleteDiary(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @PathVariable int year,
                                            @PathVariable int month,
                                            @PathVariable int day) {
        diaryService.deleteDiaryByDate(userDetails.getUserId(), year, month, day);
        return ResponseEntity.noContent().build();
    }
}
