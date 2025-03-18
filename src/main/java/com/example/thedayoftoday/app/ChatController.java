package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.DiaryBasicResponseDto;
import com.example.thedayoftoday.domain.service.AiService;

import java.io.IOException;
import org.springframework.web.bind.annotation.PostMapping;
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
    public DiaryBasicResponseDto transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException {
        String transAudio = openAiService.transcribeAudio(file);
        return openAiService.convertToDiary(transAudio);
    }
}