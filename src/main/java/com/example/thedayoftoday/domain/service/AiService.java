package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.DiaryBasicResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class AiService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String WHISPER_URL = "https://api.openai.com/v1/audio/transcriptions";
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GPT_URL = "https://api.openai.com/v1/chat/completions";
    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB 제한

    //  음성 파일을 STT 변환 (Whisper API)
    public String transcribeAudio(MultipartFile audioFile) throws IOException {
        File tempFile = convertMultipartFileToFile(audioFile);

        // MP3 파일이면 WAV로 변환
        if (audioFile.getOriginalFilename() != null && audioFile.getOriginalFilename().endsWith(".mp3")) {
            tempFile = convertMp3ToWav(tempFile);
        }

        // 파일이 25MB 초과면 FFmpeg로 분할
        if (tempFile.length() > MAX_FILE_SIZE) {
            return transcribeLargeAudio(tempFile);
        }

        return sendToWhisperApi(tempFile);
    }

    //  대용량 오디오 분할 및 STT 변환
    private String transcribeLargeAudio(File audioFile) throws IOException {
        List<File> splitFiles = splitAudioFileWithFFmpeg(audioFile);
        StringBuilder fullTranscription = new StringBuilder();

        for (File part : splitFiles) {
            String partText = sendToWhisperApi(part);
            fullTranscription.append(partText).append(" ");
        }

        return fullTranscription.toString().trim();
    }

    // Whisper API 요청 (파일 전송)
    private String sendToWhisperApi(File audioFile) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(apiKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(readFileBytes(audioFile)) {
            @Override
            public String getFilename() {
                return audioFile.getName();
            }
        });
        body.add("model", "whisper-1");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                WHISPER_URL, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> responseBody = response.getBody();
        return responseBody != null ? (String) responseBody.get("text") : "OPENAI로부터 아무런 응답이 없습니다";
    }

    // MP3 → WAV 변환 (FFmpeg 사용)
    private File convertMp3ToWav(File mp3File) throws IOException {
        File wavFile = new File(mp3File.getParent(), mp3File.getName().replace(".mp3", ".wav"));

        ProcessBuilder builder = new ProcessBuilder(
                "ffmpeg", "-i", mp3File.getAbsolutePath(), "-ar", "16000", "-ac", "1", "-c:a", "pcm_s16le",
                wavFile.getAbsolutePath()
        );
        builder.redirectErrorStream(true);
        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }
        }

        checkProcessExitCode(process);

        return wavFile;
    }

    private static void checkProcessExitCode(Process process) throws IOException {
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("FFmpeg로 MP3 파일을 WAV로 만드는데 실패");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 현재 스레드의 인터럽트 상태 유지
            throw new IOException("FFmpeg process는 현재 interrupted", e);
        }
    }

    // 오디오 파일을 FFmpeg로 25MB 이하로 분할
    private List<File> splitAudioFileWithFFmpeg(File inputFile) throws IOException {
        List<File> splitFiles = new ArrayList<>();
        String outputPattern = inputFile.getParent() + "/split_%03d.wav";

        ProcessBuilder builder = new ProcessBuilder(
                "ffmpeg", "-i", inputFile.getAbsolutePath(), "-f", "segment", "-segment_time", "30",
                "-c", "copy", outputPattern
        );
        builder.redirectErrorStream(true);
        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[FFmpeg] {}", line);
            }
        }

        checkProcessExitCode(process);

        File parentDir = inputFile.getParentFile();
        File[] files = parentDir.listFiles((dir, name) -> name.startsWith("split_") && name.endsWith(".wav"));
        if (files != null) {
            splitFiles.addAll(List.of(files));
        }
        return splitFiles;
    }

    // 텍스트를 "일기 형식"으로 변환
    public DiaryBasicResponseDto convertToDiary(String text) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are a diary-writing assistant. " +
                        "Generate a diary entry in JSON format with the keys: 'title' and 'content'."),
                Map.of("role", "user", "content", "다음 내용을 바탕으로 일기를 작성해줘:\n" + text)
        ));

        String response = callOpenAiApi(requestBody);
        return parseDiaryResponse(response);
    }

    //제목,내용 분할
    private DiaryBasicResponseDto parseDiaryResponse(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response); //response를 직렬화시켜주기위해 사용

            String title = rootNode.has("title") ? rootNode.get("title").asText() : "제목 없음";
            String content = rootNode.has("content") ? rootNode.get("content").asText() : "내용 없음";

            return new DiaryBasicResponseDto(title, content);
        } catch (Exception e) {
            log.error("일기 변환 중 오류 발생: {}", e.getMessage());
            return new DiaryBasicResponseDto("변환 오류", "일기 내용을 생성하는데 실패했습니다.");
        }
    }

    // OpenAI API 공통 호출 메서드
    private String callOpenAiApi(Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                GPT_URL, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("choices")) {
            Object choicesObj = responseBody.get("choices");

            if (choicesObj instanceof List<?> choices) {
                if (!choices.isEmpty() && choices.get(0) instanceof Map<?, ?> firstChoice) {
                    Object messageObj = firstChoice.get("message");

                    if (messageObj instanceof Map<?, ?> message) {
                        Object contentObj = message.get("content");

                        // content가 List일 경우 첫 번째 요소 가져오기 (GPT 모델이 지멋대로 바꿀 수 있어서)
                        if (contentObj instanceof List<?> contentList && !contentList.isEmpty()) {
                            Object firstContent = contentList.get(0);
                            return firstContent instanceof String ? (String) firstContent : "해당 내용을 찾을수 없습니다.";
                        }

                        return contentObj instanceof String ? (String) contentObj : "해당 내용을 찾을수 없습니다.";
                    }
                }
            }
        }
        return "응답이 없습니다.";
    }

    // MultipartFile을 File로 변환
    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("default.tmp");
        File convFile = new File(System.getProperty("java.io.tmpdir"), fileName);
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }

    public String generateNextQuestion(String answerText) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "너는 일상 회고 질문을 이어가는 대화 어시스턴트야. 간결하고 공감되는 다음 질문 하나만 생성해줘."),
                Map.of("role", "user", "content", "대답: " + answerText)
        ));

        return callOpenAiApi(requestBody);
    }


    //파일 읽어들이기
    private byte[] readFileBytes(File file) throws IOException {
        return java.nio.file.Files.readAllBytes(file.toPath());
    }
}