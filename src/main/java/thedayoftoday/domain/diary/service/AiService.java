package thedayoftoday.domain.diary.service;

import lombok.RequiredArgsConstructor;
import thedayoftoday.domain.diary.dto.DiaryContentResponseDto;
import thedayoftoday.domain.diary.moodmeter.DiaryMood;
import thedayoftoday.domain.diary.moodmeter.MoodMeter;
import thedayoftoday.domain.weeklyData.dto.WeeklyTitleFeedbackResponseDto;
import thedayoftoday.domain.weeklyData.entity.Degree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AiService {

    private final OpenAiApiClient openAiApiClient;

    private static final String NO_CONTENT = "작성된 내용이 없습니다.";
    private static final String NO_TITLE = "작성된 제목이 없습니다.";
    private static final String NO_AI_COMMENT = "작성된 AI 분석 내용이 없습니다.";
    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB

    //  음성 파일을 STT 변환 (Whisper API)
    public String transcribeAudio(MultipartFile audioFile) throws IOException {
        File tempFile = null;
        try {
            tempFile = convertMultipartFileToFile(audioFile);
            if (tempFile.length() > MAX_FILE_SIZE) {
                return transcribeLargeAudio(tempFile);
            }
            return sendToWhisperApi(tempFile);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
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
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(readFileBytes(audioFile)) {
            @Override
            public String getFilename() {
                return audioFile.getName();
            }
        });
        body.add("model", "whisper-1");
        return openAiApiClient.callWhisperApi(body);
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
    public DiaryContentResponseDto convertToDiary(String text) {
        Map<String, Object> requestBody = GptPrompt.forConvertToDiary(text);
        String response = openAiApiClient.callGptApi(requestBody);
        return parseDiaryResponse(response);
    }

    //제목,내용 분할
    private DiaryContentResponseDto parseDiaryResponse(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            String title = rootNode.has("title") ? rootNode.get("title").asText() : NO_TITLE;
            String content = rootNode.has("content") ? rootNode.get("content").asText() : NO_CONTENT;
            return new DiaryContentResponseDto(title, content);
        } catch (Exception e) {
            log.error("일기 변환 응답 파싱 실패: {}", e.getMessage());
            return new DiaryContentResponseDto("변환 오류", "일기 내용을 생성하는데 실패했습니다.");
        }
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

    public String generateNextQuestion(String conversation) {
        Map<String, Object> requestBody = GptPrompt.forGenerateNextQuestion(conversation);
        return openAiApiClient.callGptApi(requestBody);
    }

    public DiaryMood recommendMood(String diaryText) {
        if (Objects.equals(diaryText, NO_CONTENT)) {
            return new DiaryMood(MoodMeter.UNKNOWN.getMoodName(), MoodMeter.UNKNOWN.getColor());
        }

        String allowedMoods = Arrays.stream(MoodMeter.values())
                .filter(mood -> mood != MoodMeter.UNKNOWN)
                .map(MoodMeter::getMoodName)
                .map(name -> "\"" + name + "\"")
                .collect(Collectors.joining(", "));

        try {
            Map<String, Object> requestBody = GptPrompt.forRecommendMood(diaryText, allowedMoods);
            String moodName = openAiApiClient.callGptApi(requestBody).trim();
            MoodMeter moodMeter = MoodMeter.fromMoodName(moodName);
            return new DiaryMood(moodMeter.getMoodName(), moodMeter.getColor());
        } catch (Exception e) {
            log.warn("감정 추천 실패. 기본 감정으로 대체됩니다. 오류: {}", e.getMessage());
            MoodMeter defaultMood = MoodMeter.UNKNOWN;
            return new DiaryMood(defaultMood.getMoodName(), defaultMood.getColor());
        }
    }

    public String analyzeDiaryContent(String content, DiaryMood mood, Optional<String> userName) {
        if (Objects.equals(content, NO_CONTENT)) {
            return NO_AI_COMMENT;
        }
        Map<String, Object> requestBody = GptPrompt.forAnalyzeDiaryContent(content, mood, userName);
        return openAiApiClient.callGptApi(requestBody);
    }

    public WeeklyTitleFeedbackResponseDto analyzeWeeklyDiaryWithTitle(String combinedWeeklyDiary) {
        if (combinedWeeklyDiary == null || combinedWeeklyDiary.isBlank()) {
            return new WeeklyTitleFeedbackResponseDto("무기록", "해당 주간에는 작성된 일기가 없습니다.");
        }
        Map<String, Object> requestBody = GptPrompt.forAnalyzeWeeklyDiaryWithTitle(combinedWeeklyDiary);
        String response = openAiApiClient.callGptApi(requestBody);
        return parseWeeklyFeedbackResponse(response);
    }

    private WeeklyTitleFeedbackResponseDto parseWeeklyFeedbackResponse(String response) {
        try {
            String[] parts = response.split("피드백:", 2);
            String title = parts[0].replace("제목:", "").trim();

            String feedback;
            if (parts.length > 1) {
                feedback = parts[1].trim();
            } else {
                feedback = "피드백 없음";
            }

            return new WeeklyTitleFeedbackResponseDto(title, feedback);
        } catch (Exception e) {
            log.error("주간 피드백 응답 파싱 실패: {}", e.getMessage());
            return new WeeklyTitleFeedbackResponseDto("분석 실패", "GPT 응답을 이해할 수 없습니다.");
        }
    }

    public Degree analyzeDegree(String combinedWeeklyDiary) {
        if (combinedWeeklyDiary == null || combinedWeeklyDiary.isBlank()) {
            return null;
        }
        Map<String, Object> requestBody = GptPrompt.forAnalyzeDegree(combinedWeeklyDiary);
        String response = openAiApiClient.callGptApi(requestBody).trim();
        return parseDegreeResponse(response);
    }

    private Degree parseDegreeResponse(String response) {
        return switch (response) {
            case "좋은" -> Degree.GOOD;
            case "나쁜" -> Degree.BAD;
            case "편안한" -> Degree.COMFORT;
            case "힘든" -> Degree.HARD;
            default -> {
                log.warn("알 수 없는 Degree 응답: {}", response);
                yield Degree.COMFORT;
            }
        };
    }

    //파일 읽어들이기
    private byte[] readFileBytes(File file) throws IOException {
        return java.nio.file.Files.readAllBytes(file.toPath());
    }
}
