package thedayoftoday;

import thedayoftoday.dto.diary.DiaryBasicResponseDto;
import thedayoftoday.dto.diary.DiaryIdResponseDto;
import thedayoftoday.dto.diary.DiaryRequestDto;
import thedayoftoday.dto.diary.moodmeter.MoodCategoryResponse;
import thedayoftoday.entity.Diary;
import thedayoftoday.entity.DiaryMood;
import thedayoftoday.security.CustomUserDetails;
import thedayoftoday.service.AiService;
import thedayoftoday.service.DiaryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class DiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private AiService openAiService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public DiaryService diaryService() {
            return Mockito.mock(DiaryService.class);
        }

        @Bean
        @Primary
        public AiService openAiService() {
            return Mockito.mock(AiService.class);
        }
    }

    @BeforeEach
    void setupAuthentication() {
        CustomUserDetails userDetails = new CustomUserDetails("testuser@naver.com", "ROLE_USER", 1L);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createDiaryWithMood_정상요청_성공() throws Exception {
        // given
        Long userId = 1L;
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "audio.wav",
                "audio/wav",
                "fake audio".getBytes()
        );

        String transcribedText = "오늘 너무 좋았다.";
        DiaryBasicResponseDto basicResponse = new DiaryBasicResponseDto("제목", "내용");
        DiaryMood mood = new DiaryMood("행복", "#FF0000");
        DiaryIdResponseDto diaryIdResponse = new DiaryIdResponseDto(10L);
        List<MoodCategoryResponse> moodCategories = List.of();

        given(openAiService.transcribeAudio(any())).willReturn(transcribedText);
        given(openAiService.convertToDiary(anyString())).willReturn(basicResponse);
        given(openAiService.recommendMood(anyString())).willReturn(mood);
        given(diaryService.createDiary(eq(userId), eq("제목"), eq("내용"), eq(mood)))
                .willReturn(diaryIdResponse);
        given(diaryService.getAllMoodListResponseDto()).willReturn(moodCategories);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/diary/monologue/start")
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        }))
                .andExpect(status().isCreated());
    }


    @Test
    void updateMonologueDiaryMood_성공() throws Exception {
        Long diaryId = 10L;
        Long userId = 1L;

        DiaryMood mood = new DiaryMood("행복", "#FF0000");
        Diary dummyDiary = Diary.builder()
                .diaryId(diaryId)
                .title("오늘 제목")
                .content("오늘 내용")
                .build();

        doNothing().when(diaryService).updateDiaryMood(userId, diaryId, mood);
        given(diaryService.findDiaryByUserAndDiaryId(userId, diaryId)).willReturn(dummyDiary);

        mockMvc.perform(MockMvcRequestBuilders.post("/diary/update-monologue-mood")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"moodName\":\"행복\",\"color\":\"#FF0000\"}")
                        .param("diaryId", diaryId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("오늘 제목"))
                .andExpect(jsonPath("$.content").value("오늘 내용"));
    }

    @Test
    void analyzeDiary_성공() throws Exception {
        // given
        Long diaryId = 10L;
        DiaryMood mood = new DiaryMood("행복", "#FF0000");
        String analysisResult = "오늘 하루는 전반적으로 긍정적이었습니다.";

        given(diaryService.getMoodByDiaryId(diaryId)).willReturn(mood);
        given(openAiService.analyzeDiary(diaryId, mood)).willReturn(analysisResult);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/diary/analyze")
                        .param("diaryId", diaryId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(analysisResult));
    }

    @Test
    void updateDiaryContent_성공() throws Exception {
        // given
        Long userId = 1L;
        Long diaryId = 10L;
        String title = "수정된 제목";
        String content = "수정된 내용";

        DiaryRequestDto requestDto = new DiaryRequestDto(diaryId, title, content, null);

        // 인증 유저 세팅
        CustomUserDetails userDetails = new CustomUserDetails("testuser@naver.com", "ROLE_USER", userId);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String requestJson = """
        {
            "diaryId": 10,
            "title": "수정된 제목",
            "content": "수정된 내용"
        }
    """;

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.put("/diary/update-diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("일기 수정 완료"));
    }
}