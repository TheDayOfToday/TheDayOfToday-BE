package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.entity.Book;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${aladin.api.key}")
    private String aladinApiKey;

    private static final String GPT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String ALADIN_API_URL = "https://www.aladin.co.kr/ttb/api/ItemSearch.aspx";

    public void recommendBook(Long userId, String diaryContent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        String query = getBookQuery(diaryContent);

        Book book = null;
        int attempt = 0;
        while (book == null && attempt < 3) {
            try {
                book = getBookInformation(query);
            } catch (RuntimeException e) {
                log.warn("알라딘 검색 실패 ({}회차): {}", attempt + 1, e.getMessage());
                query = getBookQuery(diaryContent);
                attempt++;
            }
        }

        if (book == null) {
            throw new RuntimeException("3회 시도에도 알라딘에서 책 정보를 가져오지 못했습니다.");
        }

        user.changeRecommendedBook(book);
    }

    private String getBookQuery(String diaryContent) {
        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "system", "content",
                                "다음 일기를 보고 기존에 있는 관련된 책 하나를 추천해줘.\n" +
                                        "- 오직 추천해주고 싶은 책 제목만 한 줄로 정확히 반환해줘.\n" +
                                        "- 다른 문장이나 설명은 절대 하지 마."),
                        Map.of("role", "user", "content", diaryContent)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                GPT_URL, HttpMethod.POST, request, new ParameterizedTypeReference<>() {}
        );

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String result = ((String) message.get("content")).trim();

        log.info("GPT 추천 제목: {}", result);

        return result;
    }

    private Book getBookInformation(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = ALADIN_API_URL + "?" +
                "ttbkey=" + aladinApiKey +
                "&Query=" + encodedQuery +
                "&QueryType=Keyword" +
                "&SearchTarget=Book" +
                "&MaxResults=1" +
                "&Output=js";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String body = response.getBody();

        try {
            Map<String, Object> parsed = objectMapper.readValue(body, new TypeReference<>() {});
            List<Map<String, Object>> items = (List<Map<String, Object>>) parsed.get("item");

            if (items == null || items.isEmpty()) {
                throw new RuntimeException("알라딘에서 책 정보를 찾을 수 없습니다.");
            }

            Map<String, Object> bookData = items.get(0);
            return Book.of(
                    (String) bookData.get("title"),
                    (String) bookData.get("author"),
                    (String) bookData.get("description"),
                    (String) bookData.get("cover")
            );
        } catch (Exception e) {
            throw new RuntimeException("알라딘 응답 파싱 실패: " + e.getMessage(), e);
        }
    }
}
