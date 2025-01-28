package com.example.thedayoftoday;

import com.example.thedayoftoday.domain.AudioProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AudioProcessingServiceTest {

    @Test
    void testProcessAudio_withValidFile() throws Exception {
        AudioProcessingService service = new AudioProcessingService();

        String testFilePath = "C:\\Users\\doosa\\Desktop\\대학\\졸설\\WhisperSTT\\STORYWAY.m4a";
        File testFile = new File(testFilePath);
        assertTrue(testFile.exists(), "테스트 오디오 파일이 존재해야 합니다.");

        String result = service.processAudio(testFilePath);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> jsonResponse = mapper.readValue(result, Map.class);

        System.out.println("Transcribed Text: " + jsonResponse.get("transcribed_text"));
        System.out.println("Formal Text: " + jsonResponse.get("formal_text"));

        assertNotNull(jsonResponse.get("transcribed_text"));
        assertNotNull(jsonResponse.get("formal_text"));
    }

    @Test
    void testProcessAudio_withInvalidFile() {
        AudioProcessingService service = new AudioProcessingService();

        // 존재하지 않는 파일 경로
        String invalidFilePath = "C:\\invalid\\nonexistent_audio.wav";

        String result = service.processAudio(invalidFilePath);

        assertTrue(result.contains("error"), "잘못된 파일 처리 시 오류 메시지를 포함해야 합니다.");
    }

    @Test
    void testProcessAudio_withNoFile() {
        AudioProcessingService service = new AudioProcessingService();

        // 빈 파일 경로 테스트
        String result = service.processAudio("");

        assertTrue(result.contains("error"), "파일 경로가 없을 때 오류 메시지를 포함해야 합니다.");
    }
}