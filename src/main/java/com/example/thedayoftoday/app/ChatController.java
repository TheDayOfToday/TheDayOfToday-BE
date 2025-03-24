package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.DiaryBasicResponseDto;
import com.example.thedayoftoday.domain.dto.DiaryCreateRequestDto;
import com.example.thedayoftoday.domain.dto.conversation.ConversationResponseDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.service.AiService;

import java.io.IOException;

import com.example.thedayoftoday.domain.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AiService openAiService;

    private final ConversationService conversationService;

    public ChatController(AiService openAiService, ConversationService conversationService) {
        this.openAiService = openAiService;
        this.conversationService = conversationService;
    }

    //독백모드 음성 파일을 받아서 텍스트로 변환
    @PostMapping("/transcribe")
    public DiaryBasicResponseDto transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException {
        String transAudio = openAiService.transcribeAudio(file);
        return openAiService.convertToDiary(transAudio);
    }

    // 다음 버튼 누르면 음성 파일을 받아서 텍스트로 바꾸고 Conversation에 저장하고 텍스트 분석한 걸 바탕으로 질문 생성해서 던져줌
    @PostMapping("/next")
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
    @PostMapping("/complete")
    public DiaryBasicResponseDto completeDiary(@RequestParam("diaryId") Long diaryId) {
        return conversationService.completeDiary(diaryId);
    }
}