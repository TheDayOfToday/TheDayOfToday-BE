package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.diary.DiaryBasicResponseDto;
import com.example.thedayoftoday.domain.dto.weeklyAnalysis.WeeklyTitleFeedbackResponseDto;
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

import javax.swing.text.html.Option;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.internal.bytebuddy.asm.Advice.OffsetMapping.Target.ForField.ReadOnly;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AiService {

    private static final String NO_CONTENT = "작성된 내용이 없습니다.";
    private static final String NO_TITLE = "작성된 제목이 없습니다.";
    private static final String NO_AI_COMMENT = "작성된 AI 분석 내용이 없습니다.";
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
        File tempFile = null;
        try {
            tempFile = convertMultipartFileToFile(audioFile);

            // 25MB 이상이면 분할 처리
            if (tempFile.length() > MAX_FILE_SIZE) {
                return transcribeLargeAudio(tempFile);
            }

            return sendToWhisperApi(tempFile);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("임시 음성 파일 삭제 실패: {}", tempFile.getAbsolutePath());
                }
            }
        }
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

    private static void checkProcessExitCode(Process process) throws IOException {
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("FFmpeg로 MP3 파일을 m4a로 만드는데 실패");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 현재 스레드의 인터럽트 상태 유지
            throw new IOException("FFmpeg process는 현재 interrupted", e);
        }
    }

    // 오디오 파일을 FFmpeg로 25MB 이하로 분할
    private List<File> splitAudioFileWithFFmpeg(File inputFile) throws IOException {
        List<File> splitFiles = new ArrayList<>();
        String outputPattern = inputFile.getParent() + "/split_%03d.m4a";

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
        File[] files = parentDir.listFiles((dir, name) -> name.startsWith("split_") && name.endsWith(".m4a"));
        if (files != null) {
            splitFiles.addAll(List.of(files));
        }
        return splitFiles;
    }

    // 텍스트를 "일기 형식"으로 변환
    public DiaryBasicResponseDto convertToDiary(String text) {

        if (!checkTextLength(text)) {
            return new DiaryBasicResponseDto(NO_TITLE, NO_CONTENT);
        }

        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", """
                            너는 사람의 음성을 바탕으로 감정적인 한국어 일기를 작성해주는 도우미야.
                        
                            사용자는 하루 동안 겪은 일이나 감정을 음성으로 털어놓았고,
                            너는 그 음성을 텍스트로 변환한 내용을 기반으로 일기를 써줘야 해.
                        
                            다음 조건을 꼭 지켜:
                            - 출력 형식은 반드시 JSON 형식이어야 하며, 다음 두 키만 포함해야 해:
                              - title: 일기의 감정을 상징적으로 표현한 10자 이내 제목
                              - content: 실제 일기처럼 감정이 잘 드러나는 본문
                            - 사용자가 말한 분량(길이)에 따라 자연스럽게 본문 길이를 조절해서 작성
                            - 문장은 과거형 일기 문체로 작성해줘 ("~했다", "~였다" 등)
                            - 설명, 주석, 텍스트 없이 오직 JSON만 출력해
                        """),
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

            String title = rootNode.has("title") ? rootNode.get("title").asText() : NO_TITLE;
            String content = rootNode.has("content") ? rootNode.get("content").asText() : NO_CONTENT;

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
        String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("default.m4a");
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
        if (Objects.equals(diaryText, NO_CONTENT)) {
            MoodMeter defaultMood = MoodMeter.UNKNOWN;
            return new DiaryMood(defaultMood.getMoodName(), defaultMood.getColor());
        }

        String allowedMoods = Arrays.stream(MoodMeter.values())
                .filter(mood -> mood != MoodMeter.UNKNOWN)
                .map(MoodMeter::getMoodName)
                .map(name -> "\"" + name + "\"")
                .collect(Collectors.joining(", "));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", """
                            다음 일기 내용을 읽고 아래 감정 리스트 중 '모르겠는'을 제외한 정확히 하나의 감정을 반드시 예외없이 한국어로만 반환해줘.
                            아래 규칙을 반드시 지켜:
                            - 반드시 예외없이 감정 리스트에 없는 감정은 절대 말하지 마.
                            - 반드시 예외없이 '모르겠는'이라는 단어는 절대 출력하지 마.
                            - 반드시 예외없이 감정 이름 외에 다른 문장이나 설명은 하지 마.
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

        Optional<String> userName = diaryRepository.findUserNameByDiaryId(diaryId);

        if (Objects.equals(diary.getContent(), NO_CONTENT)) {
            return NO_AI_COMMENT;
        }
        String result = callGptForAnalysis(diary.getContent(), mood, userName);
        diary.addAnalysisContent(result);

        return result;
    }

    private String callGptForAnalysis(String diaryContent, DiaryMood moodName, Optional<String> userName) {
        String name = userName.orElse("사용자");

        String systemMessage = """
                 다음 일기 내용을 읽고, 다음 기준에 따라 분석을 해줘.
                - 분석 대상은 %s님이야 반드시 이 이름+님으로 글을 시작해줘.
                - 반드시 한 편의 짧은 글처럼 써줘. (9문장 정도)
                - 모든 문장에는 예외없이 반드시 존댓말로 써줘.
                - 처음엔 어떤 기분일지 말하고, 그 다음에 그렇게 생각한 이유를 분석해서 글을 써줘.
                - 결과 앞에 'analysis:' 같은 키나 구분 문구는 절대로 붙이지 마.
                - JSON 형식이나 리스트 형식 절대 사용하지 말고, 오직 한 문단의 자연스러운 글만 출력해.
                - 마지막 문구는 자연스럽게 분석을 끝내는 것 처럼 작성해줘.
                - 반드시 예외없이 '~겠죠?', '~일까요?', '~죠.' 같은 말투는 쓰지 말고, 단정적인 정중한 말로만 써줘.
                """.formatted(name);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemMessage),
                Map.of("role", "user", "content", diaryContent)
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
                피드백: 감정 흐름에 대한 피드백, 최소 6문장 이상
                
                일기 모음:
                %s
                """.formatted(combinedWeeklyDiary);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "너는 감정 분석에 능숙한 피드백 작성 도우미야."),
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
                Map.of("role", "system", "content",
                        "You are an emotional analyzer. Return only one word: 좋은, 나쁜, 편안한, 힘든. "),
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

    boolean checkTextLength(String text) {
        String[] words = text.split("\\s+");
        return words.length >= 8;
    }
}
