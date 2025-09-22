package thedayoftoday.domain.book.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import thedayoftoday.domain.auth.security.CustomUserDetails;
import thedayoftoday.domain.book.dto.RecommendedBookResponseDto;
import thedayoftoday.domain.book.service.BookService;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @InjectMocks
    private BookController bookController;

    @Mock
    private BookService bookService;

    /**
     * @AuthenticationPrincipal CustomUserDetails 를 컨트롤러 파라미터로 주입하기 위한 최소 Resolver.
     */
    private MockMvc mockMvcWith(CustomUserDetails userDetails) {
        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(CustomUserDetails.class);
            }
            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return userDetails;
            }
        };
        return MockMvcBuilders
                .standaloneSetup(bookController)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();
    }

    @Test
    @DisplayName("POST /book/recommend")
    void recommendBook_Success() throws Exception {
        // given
        long diaryId = 1L;
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        doNothing().when(bookService).recommendBookToDiary(diaryId);

        // when & then
        mockMvcWith(userDetails)
                .perform(post("/book/recommend")
                        .param("diaryId", String.valueOf(diaryId))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        // then
        verify(bookService).recommendBookToDiary(diaryId);
    }

    @Test
    @DisplayName("GET /book/show")
    void getRecommendedBook_Success() throws Exception {
        // given
        long userId = 10L;
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        given(userDetails.getUserId()).willReturn(userId);

        RecommendedBookResponseDto dto =
                new RecommendedBookResponseDto("제목", "저자", "설명", "cover.jpg");

        given(bookService.getRecommendedBookForUser(userId)).willReturn(dto);

        // when & then
        mockMvcWith(userDetails)
                .perform(get("/book/show"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.author").value("저자"))
                .andExpect(jsonPath("$.description").value("설명"))
                .andExpect(jsonPath("$.coverImageUrl").value("cover.jpg"));

        // then
        verify(bookService).getRecommendedBookForUser(userId);
    }
}
