package thedayoftoday.domain.diary.service; // 또는 infrastructure.openai 같은 패키지로

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiApiClient {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    private static final String GPT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String WHISPER_URL = "https://api.openai.com/v1/audio/transcriptions";

    public String callGptApi(Map<String, Object> requestBody) {
        HttpHeaders headers = createJsonHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                GPT_URL, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {
                }
        );

        return parseGptResponse(response.getBody());
    }

    public String callWhisperApi(MultiValueMap<String, Object> requestBody) {
        HttpHeaders headers = createMultipartHeaders();
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                WHISPER_URL, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> responseBody = response.getBody();
        return responseBody != null ? (String) responseBody.get("text") : "OPENAI로부터 아무런 응답이 없습니다";
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }

    private HttpHeaders createMultipartHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(apiKey);
        return headers;
    }

    private String parseGptResponse(Map<String, Object> responseBody) {
        if (responseBody != null && responseBody.get("choices") instanceof List<?> choices && !choices.isEmpty()) {
            if (choices.get(0) instanceof Map<?, ?> firstChoice && firstChoice.get("message") instanceof Map<?, ?> message) {
                if (message.get("content") instanceof String content) {
                    return content;
                }
            }
        }
        return "응답이 없습니다.";
    }
}