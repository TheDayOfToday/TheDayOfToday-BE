package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.diary.DiaryBasicResponseDto;
import com.example.thedayoftoday.domain.dto.diary.DiaryContentDto;
import com.example.thedayoftoday.domain.dto.diary.DiaryRequestDto;
import com.example.thedayoftoday.domain.dto.diary.RecommendRequestDto;
import com.example.thedayoftoday.domain.dto.diary.conversation.ConversationResponseDto;
import com.example.thedayoftoday.domain.dto.diary.moodmeter.MoodCategoryResponse;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.DiaryMood;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import com.example.thedayoftoday.domain.security.CustomUserDetails;
import com.example.thedayoftoday.domain.service.AiService;

import java.io.IOException;

import com.example.thedayoftoday.domain.service.ConversationService;
import com.example.thedayoftoday.domain.service.DiaryService;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/diary")
public class DiaryController {

    private final AiService openAiService;
    private final ConversationService conversationService;
    private final DiaryService diaryService;
    private final DiaryRepository diaryRepository;

    public DiaryController(AiService openAiService, ConversationService conversationService,
                           DiaryService diaryService, DiaryRepository diaryRepository) {
        this.openAiService = openAiService;
        this.conversationService = conversationService;
        this.diaryService = diaryService;
        this.diaryRepository = diaryRepository;
    }

    //독백모드 버튼
    @PostMapping(value = "/monologue/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createDiaryWithMood(@RequestParam("file") MultipartFile file,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails)
            throws IOException {
        Long userId = userDetails.getUserId();

        DiaryRequestDto emptyDiary = diaryService.createEmptyDiary(userId);

        String transcribedText = openAiService.transcribeAudio(file);
        DiaryBasicResponseDto diary = openAiService.convertToDiary(transcribedText);

        diaryService.updateDiaryContent(userId, emptyDiary.diaryId(), diary.title(), diary.content());

        return ResponseEntity.ok("음성이 성공적으로 분석되었습니다.");
    }

    @GetMapping("/update-mood")
    public RecommendRequestDto showDiaryMood(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestParam(value = "diaryId") Long diaryId) {
        long userId = userDetails.getUserId();

        Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new IllegalArgumentException("해당 일기가 없습니다"));

        if (!Objects.equals(diary.getUser().getUserId(), userId)) {
            throw new AccessDeniedException("자신의 일기만 조회할 수 있습니다.");
        }

        String transcribedText = diary.getContent();
        DiaryMood mood = openAiService.recommendMood(transcribedText);

        List<MoodCategoryResponse> moodCategories = diaryService.getAllMoodListResponseDto();

        return new RecommendRequestDto(mood, moodCategories);
    }

    //사용자가 감정 선택
    @PostMapping("/update-mood")
    public ResponseEntity<String> updateDiaryMood(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @RequestParam(value = "diaryId") Long diaryId,
                                                  @RequestBody DiaryMood mood) {
        Long userId = userDetails.getUserId();
        diaryService.updateDiaryMood(userId, diaryId, mood);
        return ResponseEntity.ok("감정이 성공적으로 저장되었습니다.");
    }

    //사용자가 일기 수정
    @PutMapping("/update-diary")
    public ResponseEntity<String> updateDiaryContent(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @RequestBody DiaryContentDto diaryContentDto) {
        Long userId = userDetails.getUserId();
        diaryService.updateDiaryContent(userId, diaryContentDto.diaryId(), diaryContentDto.title(), diaryContentDto.content());
        return ResponseEntity.ok("일기 수정 완료");
    }

    //사용자 무드미터, 일기 토대로 감정 분석
    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeDiary(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @RequestParam(value = "diaryId") Long diaryId) {

        Long userId = userDetails.getUserId();

        DiaryMood mood = diaryService.getMoodByDiaryId(diaryId);

        String analysis = openAiService.analyzeDiary(diaryId, mood);
        diaryService.updateAnalysisContent(userId, diaryId, analysis);
        return ResponseEntity.ok(analysis);
    }

    //대화모드 시작 버튼
    @PostMapping("/conversation-mode/start")
    public ResponseEntity<DiaryRequestDto> startConversation(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        DiaryRequestDto diaryResponseDto = diaryService.createEmptyDiary(userId);
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
    public ResponseEntity<DiaryRequestDto> completeDiary(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                         @RequestParam("diaryId") Long diaryId) {
        Long userId = userDetails.getUserId();
        String mergedText = conversationService.mergeConversationText(diaryId);
        DiaryBasicResponseDto diary = openAiService.convertToDiary(mergedText);
        DiaryMood mood = openAiService.recommendMood(diary.content());
        diaryService.updateDiaryContent(userId, diaryId, diary.title(), diary.content());
        return ResponseEntity.ok(new DiaryRequestDto(diaryId, diary.title(), diary.content(), mood));
    }

    //일기 삭제
    @DeleteMapping("/delete/{diaryId}")
    public ResponseEntity<String> deleteDiary(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @PathVariable Long diaryId) {
        Long userId = userDetails.getUserId();
        diaryService.deleteDiary(userId, diaryId);
        return ResponseEntity.ok("삭제가 완료되었습니다.");
    }
}
