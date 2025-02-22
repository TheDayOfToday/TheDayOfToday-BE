package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.service.AudioProcessingService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/audio")
public class AudioController {

    private final AudioProcessingService audioProcessingService;

    @Autowired
    public AudioController(AudioProcessingService audioProcessingService) {
        this.audioProcessingService = audioProcessingService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadAudio(@RequestParam("file") MultipartFile file) {
        try {
            // 업로드된 파일을 임시 디렉토리에 저장
            File tempFile = File.createTempFile("uploaded_", ".wav");
            file.transferTo(tempFile);

            // Python 코드 실행
            String response = audioProcessingService.processAudio(tempFile.getAbsolutePath());

            // 임시 파일 삭제
            tempFile.delete();

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("{\"error\":\"File upload failed\"}");
        }
    }
}
