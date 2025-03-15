package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.DiaryRequestDto;
import com.example.thedayoftoday.domain.service.AiService;
import java.io.IOException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AiService openAiService;

    public ChatController(AiService openAiService) {
        this.openAiService = openAiService;
    }

    // 음성 파일을 받아서 텍스트로 변환
    @PostMapping("/transcribe")
    public String transcribeAudio(@RequestParam("file") MultipartFile file) {
        try {
            return openAiService.transcribeAudio(file);
        } catch (IOException e) {
            return "파일 오류: " + e.getMessage();
        }
    }

    // 텍스트를 일기 형식으로 변환
    @PostMapping("/diary")
    public DiaryRequestDto convertToDiary(@RequestBody String text) {
        return openAiService.convertToDiary(text);
    }

    // 텍스트 감정 분석
    @PostMapping("/emotion")
    public String analyzeEmotion(@RequestBody String text) {
        return openAiService.analyzeTextEmotion(text);
    }

}