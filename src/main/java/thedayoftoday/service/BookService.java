package thedayoftoday.service;

import thedayoftoday.entity.Book;
import thedayoftoday.entity.User;
import thedayoftoday.repository.UserRepository;
import thedayoftoday.external.AladinXmlHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final UserRepository userRepository;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${aladin.api.key}")
    private String aladinApiKey;

    private static final String ALADIN_API_URL = "https://www.aladin.co.kr/ttb/api/ItemSearch.aspx";
    private static final List<String> BESTSELLER_TITLES = List.of(
            "역행자", "세이노의 가르침", "불편한 편의점", "미드나잇 라이브러리", "하얼빈",
            "흔한남매", "도둑맞은 집중력", "나는 메트로폴리탄 미술관의 경비원입니다", "이기적 유전자", "우리는 모두 죽는다는 것을 잊고 있었다",
            "달러구트 꿈 백화점", "불안", "지구 끝의 온실", "작별인사", "죽고 싶지만 떡볶이는 먹고 싶어",
            "모순", "완전한 행복", "브람스를 좋아하세요", "고양이", "아몬드",
            "정의란 무엇인가", "사피엔스", "총, 균, 쇠", "인간 실격", "나는 나로 살기로 했다",
            "참을 수 없는 존재의 가벼움", "호밀밭의 파수꾼", "월든", "데미안", "멋진 신세계"
    );

    public void recommendBook(Long userId, String diaryContent, String analysisContent) {
        log.info("책 추천 시작 - userId: {}", userId);

        for (int i = 1; i <= 5; i++) {
            try {
                log.info("GPT 추천 시도 {}회차", i);
                String title = extractTitleFromGpt(diaryContent, analysisContent);
                log.info("GPT 응답 책 제목: {}", title);

                Book book = searchBookFromAladin(title);
                saveBookToUser(userId, book);
                return;

            } catch (Exception e) {
                log.warn("GPT or 알라딘 실패 {}회차 - {}", i, e.getMessage());
            }
        }

        log.info("GPT 추천 실패. 베스트셀러 목록에서 랜덤 추천으로 대체합니다.");
        String backupTitle = BESTSELLER_TITLES.get(new Random().nextInt(BESTSELLER_TITLES.size()));
        try {
            Book book = searchBookFromAladin(backupTitle);
            saveBookToUser(userId, book);
        } catch (Exception e) {
            log.error("백업 추천 도서 조회 실패 - {}", e.getMessage());
            throw new RuntimeException("도서 추천에 실패했습니다.");
        }
    }

    private Book searchBookFromAladin(String rawTitle) throws Exception {
        String cleanedTitle = rawTitle
                .replaceAll("[\"'()]", "")
                .replaceAll("\\s*by\\s+.*", "")
                .trim();

        log.info("알라딘 검색용 정제 제목: {}", cleanedTitle);

        String encoded = URLEncoder.encode(cleanedTitle, StandardCharsets.UTF_8);
        String url = ALADIN_API_URL +
                "?ttbkey=" + aladinApiKey +
                "&Query=" + encoded +
                "&QueryType=Title" +
                "&SearchTarget=Book" +
                "&MaxResults=1" +
                "&Output=xml" +
                "&Cover=Big" +
                "&Version=20131101";

        log.info("알라딘 API 요청 URL: {}", url);

        AladinXmlHandler handler = new AladinXmlHandler();
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(new URL(url).openStream(), handler);

        List<Map<String, String>> items = handler.getItems();
        if (items.isEmpty()) {
            throw new RuntimeException("알라딘에서 책을 찾을 수 없습니다.");
        }

        Map<String, String> first = items.get(0);
        return Book.of(
                first.getOrDefault("title", "제목 없음"),
                first.getOrDefault("author", "저자 없음"),
                first.getOrDefault("description", "설명 없음"),
                first.getOrDefault("cover", null)
        );
    }

    private void saveBookToUser(Long userId, Book book) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        user.changeRecommendedBook(book);
        userRepository.save(user);
        log.info("사용자에게 추천 도서 저장 완료 - userId: {}", userId);
    }

    private String extractTitleFromGpt(String diary, String analysis) {
        for (int i = 1; i <= 5; i++) {
            try {
                String prompt = String.format(
                        "너는 감정 기반 독서 추천 전문가야.\n" +
                                "다음은 사용자의 감정 일기와 분석 내용이야. 이 사용자에게 어울리는 실제 존재하는 책을 한 권 추천해줘.\n\n" +
                                "- 다른 문장이나 설명은 절대 하지 마. 쌍따옴표같은 특수문자도 넣지 마\n" +
                                "알라딘에 검색하면 나오는 실제로 존재하는 책의 제목이어야 해\n" +
                                "- 오직 추천해주고 싶은 책 제목만 한 줄로 정확히 반환해줘.\n" +
                                "일기: %s\n\n" +
                                "분석: %s\n\n" +
                                "정확한 책 제목만 한 줄로 반환해. 작가명, 설명, 추천 이유는 포함하지 마.", diary, analysis
                );

                Map<String, Object> message = Map.of("role", "user", "content", prompt);
                Map<String, Object> requestBody = Map.of(
                        "model", "gpt-3.5-turbo",
                        "messages", List.of(message)
                );

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(openAiApiKey);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                RestTemplate restTemplate = new RestTemplate();

                ResponseEntity<Map> response = restTemplate.exchange(
                        "https://api.openai.com/v1/chat/completions",
                        HttpMethod.POST,
                        entity,
                        Map.class
                );

                Map<String, Object> choice = ((List<Map<String, Object>>) response.getBody().get("choices")).get(0);
                Map<String, Object> messageMap = (Map<String, Object>) choice.get("message");
                return ((String) messageMap.get("content")).trim();

            } catch (Exception e) {
                log.warn("GPT 추천 실패 {}회차 - {}", i, e.getMessage());
            }
        }
        throw new RuntimeException("GPT 책 제목 추천 5회 실패");
    }
}
