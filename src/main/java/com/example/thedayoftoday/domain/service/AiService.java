package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.DiaryBasicResponseDto;
import com.example.thedayoftoday.domain.dto.WeeklyTitleFeedbackResponseDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.DiaryMood;
import com.example.thedayoftoday.domain.entity.enumType.Degree;
import com.example.thedayoftoday.domain.entity.enumType.MoodMeter;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
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

    private final DiaryRepository diaryRepository;

    public AiService(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }

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
                Map.of("role", "user", "content", "다음 내용을 바탕으로 한국어로 일기를 작성해줘:\n" + text)
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
                Map.of("role", "system", "content", "이 질문에 대해서 한국어로 대답해주면서 간결하고 공감되는 다음 질문 하나만 생성해줘."),
                Map.of("role", "user", "content", "대답: " + answerText)
        ));

        return callOpenAiApi(requestBody);
    }

    public DiaryMood recommendMood(String diaryText) {
        String allowedMoods = Arrays.stream(MoodMeter.values())
                .map(MoodMeter::getMoodName)
                .map(name -> "\"" + name + "\"")
                .collect(Collectors.joining(", "));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", """
                다음 일기 내용을 분석해서 아래 리스트 중 감정 하나만 골라서 한국어로 반환해줘.
                반드시 리스트에 있는 감정 중 하나만 말해줘. 다른 말은 하지 마.
                감정 리스트: [%s]
            """.formatted(allowedMoods)),
                Map.of("role", "user", "content", diaryText)
        ));

        try {
            String moodName = callOpenAiApi(requestBody).trim();
            MoodMeter moodMeter = MoodMeter.fromMoodName(moodName);
            return new DiaryMood(moodMeter.getMoodName(), moodMeter.getColor());

        } catch (Exception e) {
            log.warn("감정 추천 실패. 평범함 감정으로 대체됨. 오류: {}", e.getMessage());
            MoodMeter defaultMood = MoodMeter.UNKNOWN;
            return new DiaryMood(defaultMood.getMoodName(), defaultMood.getColor());
        }
    }

    @Transactional
    public String analyzeDiary(Long diaryId, DiaryMood mood) {
        Diary diary = diaryRepository.findByDiaryId(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 다이어리가 존재하지 않습니다."));

        String prompt = """
                아래는 사용자의 일기입니다.
                사용자가 선택한 감정은 [%s]입니다. 이 감정을 반영하여 아래 일기를 분석해줘.
                감정의 원인, 사용자 성향, 긍정적 마무리 코멘트 등을 한국어로 6문장으로 작성해줘.
                        
                일기 내용:
                %s
                """.formatted(mood.getMoodName(), diary.getContent());

        String result = callGptForAnalysis(prompt);
        diary.addAnalysisContent(result);

        return result;
    }

    private String callGptForAnalysis(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "다음 내용을 분석해서 감정의 원인, 성향, 긍정적 코멘트를 한국어로 6문장으로 정리해줘."),
                Map.of("role", "user", "content", prompt)
        ));
        return callOpenAiApi(requestBody);
    }
    public WeeklyTitleFeedbackResponseDto analyzeWeeklyDiaryWithTitle(String combinedWeeklyDiary) {
        if (combinedWeeklyDiary == null || combinedWeeklyDiary.isBlank()) {
            return new WeeklyTitleFeedbackResponseDto("무기록", "해당 주간에는 작성된 일기가 없습니다.");
        }

        String prompt = """
    아래는 사용자의 일주일간 일기 모음입니다.
    각 일기에는 사용자가 선택한 감정이 포함되어 있습니다.

    이 일기를 분석하여 감정의 흐름, 성향, 감정 변화의 원인 등을 바탕으로
    아래 형식처럼 작성해주세요:

    제목: 짧고 상징적인 한 줄 제목
    피드백: 감정 흐름에 대한 공감 가는 피드백, 최소 6문장 이상

    일기 모음:
    %s
    """.formatted(combinedWeeklyDiary);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "너는 공감과 감정 분석에 능숙한 피드백 작성 도우미야."),
                Map.of("role", "user", "content", prompt)
        ));

        String response = callOpenAiApi(requestBody);

        try {
            String[] parts = response.split("피드백:", 2);
            String title = parts[0].replace("제목:", "").trim();
            String feedback = parts.length > 1 ? parts[1].trim() : "피드백 없음";
            return new WeeklyTitleFeedbackResponseDto(title, feedback);
        } catch (Exception e) {
            log.error("응답 파싱 실패: {}", e.getMessage());
            return new WeeklyTitleFeedbackResponseDto("분석 실패", "GPT 응답을 이해할 수 없습니다.");
        }
    }

    public Degree analyzeDegree(String combinedWeeklyDiary) {
        if (combinedWeeklyDiary == null || combinedWeeklyDiary.isBlank()) {
            return Degree.NONE; // 기본값 처리
        }

        String prompt = """
        다음은 사용자의 일주일간 일기 모음입니다.

        전체 감정 흐름을 종합해 다음 중 하나로 판단해주세요:
        - 좋은
        - 나쁜
        - 편안한
        - 힘든

        반드시 위 단어 중 하나만 한국어로 대답해주세요.

        일기 모음:
        %s
        """.formatted(combinedWeeklyDiary);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are an emotional analyzer. Return only one word: 좋은, 나쁜, 편안한, 힘든. "),
                Map.of("role", "user", "content", prompt)
        ));

        String response = callOpenAiApi(requestBody).trim();

        return switch (response) {
            case "좋은" -> Degree.GOOD;
            case "나쁜" -> Degree.BAD;
            case "편안한" -> Degree.COMFORT;
            case "힘든" -> Degree.HARD;
            default -> Degree.NONE; // 잘못된 응답 처리
        };
    }
    //파일 읽어들이기
    private byte[] readFileBytes(File file) throws IOException {
        return java.nio.file.Files.readAllBytes(file.toPath());
    }
}