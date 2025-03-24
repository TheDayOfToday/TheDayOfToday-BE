package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.AudioResponseDto;
import com.example.thedayoftoday.domain.service.AudioService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/audio")
public class AudioController {

    private final AudioService audioService;

    @Autowired
    public AudioController(AudioService audioService) {
        this.audioService = audioService;
    }

    @PostMapping("/upload")
    public ResponseEntity<AudioResponseDto> uploadAudio(@RequestParam("file") MultipartFile file) {
        File tempFile = null;
        try {
            // 업로드된 파일을 임시 디렉토리에 저장
            tempFile = File.createTempFile("uploaded_", ".wav");
            Files.copy(file.getInputStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Python 코드 실행
            AudioResponseDto response = audioService.processAudio(tempFile);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(AudioResponseDto.failure("File upload failed"));
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
