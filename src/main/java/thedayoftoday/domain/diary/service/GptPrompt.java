package thedayoftoday.domain.diary.service;

import thedayoftoday.domain.diary.moodmeter.DiaryMood;

import java.util.List;
import java.util.Map;
import java.util.Optional;

//GPT API 요청을 위한 프롬프트와 Request Body를 생성하는 유틸리티 클래스
public final class GptPrompt {

    private static final String GPT_MODEL = "gpt-3.5-turbo";

    private GptPrompt() {}

    // 일반 텍스트를 일기 형식(제목, 내용)으로 변환하기 위한 프롬프트를 생성
    public static Map<String, Object> forConvertToDiary(String text) {
        String systemContent = """
                너는 사람의 음성을 바탕으로 감정적인 한국어 일기를 작성해주는 도우미야.
            
                사용자는 하루 동안 겪은 일이나 감정을 음성으로 털어놓았고,
                너는 그 음성을 텍스트로 변환한 내용을 기반으로 자연스러운 일기로 변환해야 해.
            
                다음 조건을 꼭 지켜:
                - 출력 형식은 반드시 JSON 형식이어야 하며, 다음 두 키만 포함해야 해:
                  - title: 일기의 감정을 상징적으로 표현한 10자 이내 제목
                  - content: 실제 일기처럼 감정이 잘 드러나는 본문
                - 사용자가 말한 분량(길이)에 따라 자연스럽게 본문 길이를 조절해서 작성
                - 중간중간 잡음으로 인해 입력된 것 같은 텍스트는 반영하면 안됨
                - 문장은 과거형 일기 문체로 작성해줘 ("~했다", "~였다" 등)
                - 설명, 주석, 텍스트 없이 오직 JSON만 출력해
            """;

        return Map.of(
                "model", GPT_MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", systemContent),
                        Map.of("role", "user", "content", "다음 내용을 바탕으로 한국어로 자연스럽게 일기를 작성해줘:\n" + text)
                )
        );
    }

    //대화 내용을 바탕으로 다음 질문을 생성하기 위한 프롬프트
    public static Map<String, Object> forGenerateNextQuestion(String conversation) {
        String systemContent = """
                너는 상대방의 말을 잘 들어주는 심리 상담사야. 해당 대화 내용을 보고 다음에 오면 자연스러울 것 같은 간결한 한 줄짜리 대답과 질문 하나를 만들어줘.
                - 실제 상담사가 내담자와 대화하듯이 해야해. 즉, 상대 말에 공감을 잘 해야해.
                - ‘어떤 것이 궁금하신가요’ 같은 말은 절대 하지 말고, 진짜 친구가 따뜻하게 물어보듯 자연스럽게 말해줘.
                - 질문이라는 단어도 사용하지 마. 대화가 자연스럽게 이어질 수 있도록 말해줘.
                - 출력 양식은 질문 그 자체 text만 나와야해. 다른 특수문자나 '질문 :' 같은 설명은 없어야 해.
                - 이전에 했던 질문들은 사용하면 절대 안돼.
                """;

        return Map.of(
                "model", GPT_MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", systemContent),
                        Map.of("role", "user", "content", "대화 내용: " + conversation)
                )
        );
    }

    //일기 내용을 바탕으로 사용자의 감정을 추천하기 위한 프롬프트
    public static Map<String, Object> forRecommendMood(String diaryText, String allowedMoods) {
        String systemContent = """
                너는 일기를 읽고 일기 작성자의 감정을 정확하게 알아내는 전문가야.
                다음 일기 내용을 읽고 아래 감정 리스트 중 정확히 하나의 감정을 반드시 예외없이 한국어로만 반환해줘.
                아래 규칙을 반드시 지켜:
                - 반드시 예외없이 감정 리스트에 없는 감정은 절대 말하지 마.
                - 모르겠음 감정도 절대 말하지 마
                - 반드시 예외없이 감정 이름 외에 다른 문장이나 설명은 하지 마.
                감정 리스트: [%s]
            """.formatted(allowedMoods);

        return Map.of(
                "model", GPT_MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", systemContent),
                        Map.of("role", "user", "content", diaryText)
                )
        );
    }

    //일기 내용과 감정을 바탕으로 심층 분석 피드백을 생성하기 위한 프롬프트
    public static Map<String, Object> forAnalyzeDiaryContent(String diaryContent, DiaryMood mood, Optional<String> userName) {
        String name = userName.orElse("사용자");
        String systemContent = """
                다음 일기 내용을 읽고, 다음 기준에 따라 분석을 해줘.
                - 분석 대상은 %s님이야. 반드시 이 이름+님으로 글을 시작해줘.
                - 반드시 한 편의 짧은 글처럼 써줘. (9문장 정도)
                - 모든 문장은 예외없이 반드시 존댓말로 써줘.
                - 처음엔 어떤 기분일지 말하고, 그 다음에 그렇게 생각한 이유를 분석해서 글을 써줘.
                - 결과 앞에 'analysis:' 같은 키나 구분 문구는 절대로 붙이지 마.
                - JSON 형식이나 리스트 형식 절대 사용하지 말고, 오직 한 문단의 자연스러운 글만 출력해.
                - 마지막 문구는 자연스럽게 분석을 끝내는 것 처럼 작성해줘.
                - 반드시 예외없이 '~겠죠?', '~일까요?', '~죠.' 같은 말투는 쓰지 말고, 단정적인 정중한 말로만 써줘.
                """.formatted(name);

        return Map.of(
                "model", GPT_MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", systemContent),
                        Map.of("role", "user", "content", "선택된 감정: " + mood.getMoodName() + "\n일기 내용: " + diaryContent)
                )
        );
    }

    //주간 일기 모음을 바탕으로 제목과 피드백을 생성하기 위한 프롬프트
    public static Map<String, Object> forAnalyzeWeeklyDiaryWithTitle(String combinedWeeklyDiary) {
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

        return Map.of(
                "model", GPT_MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", "너는 감정 분석에 능숙한 피드백 작성 도우미야."),
                        Map.of("role", "user", "content", prompt)
                )
        );
    }

    //주간 일기 모음을 바탕으로 전반적인 감정의 '정도(Degree)'를 분석하기 위한 프롬프트
    public static Map<String, Object> forAnalyzeDegree(String combinedWeeklyDiary) {
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

        return Map.of(
                "model", GPT_MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content",
                                "You are an emotional analyzer. Return only one word: 좋은, 나쁜, 편안한, 힘든. "),
                        Map.of("role", "user", "content", prompt)
                )
        );
    }
}