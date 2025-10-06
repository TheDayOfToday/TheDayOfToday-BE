package thedayoftoday.domain.diary.service;

import thedayoftoday.domain.diary.moodmeter.DiaryMood;

import java.util.List;
import java.util.Map;
import java.util.Optional;

//GPT API 요청을 위한 프롬프트와 Request Body를 생성하는 유틸리티 클래스
public final class GptPrompt {

    private static final String GPT_MODEL = "gpt-4o-mini";

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
                
                [절대 금지]
                - 원문에 없는 사실/사람/장소/사건/시간/원인 추정, 성향 단정, 과장 위로/예언(“분명 ~할 것입니다”, “행복이 가득할 것입니다” 등).
                - '함께 응원/함께 힘' 같은 **함께 화법**.
                - 의학적/심리 진단, 강한 단정(“반드시/절대로”).
                - 수치/등급/퍼센트/점수/지수/순위 등 **계량 표현**.
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
        너는 **공감적 톤의 감정 분석 도우미**다. 아래 주간 일기 원문(한 주 모든 일기를 합친 하나의 텍스트)만을 **유일한 근거**로 사용하여 결과를 작성하라.

        [목표]
        - '제목'(한 줄)과 '피드백'(최소 6문장)을 생성한다.
        - 톤은 따뜻하고 공감적이되, 과장·추측·진단 없이 **원문 근거 중심**으로 서술한다.

        [절대 규칙]
        1) **원문에 없는 정보(인물/날짜/사건/장소/심리 진단/해석어)**를 새로 만들지 말 것.
        2) 어떤 요약/원인/경향/조언도 **원문에 있는 표현이나 묘사**에 근거해야 한다.
           - 근거가 애매하면 그 문장은 **삭제**한다.
           - “~라고 적으신 부분을 보면”, “~라고 표현하신 대목에서”처럼 **참조**만 하되, 과한 직접 인용/작위적 인용부호 남발은 피한다.
        3) **수치/등급/지수/퍼센트/점수/순위/상·중·하** 등 계량 표현 금지. (원문에 실제 숫자가 있어도 통계처럼 해석하지 말 것)
        4) 의학적·심리진단 어휘, 단정적 판단(“반드시/절대로”), 과도한 해석 금지.
        5) 이번 주 흐름을 정리할 때, 같은 주 안에서도 **최근에 가까운 내용**을 조금 더 우선시한다. (다만 과거 내용을 무시하지는 말 것)
        6) 피드백 구성은 다음 순서를 따를 것:
           (a) 이번 주 **원문에서 드러난** 감정 흐름의 간결한 요약
           (b) 그렇게 느낀 **근거가 된 대목** 1~2개를 자연스럽게 참조
           (c) 스스로를 격려하는 **공감 문장** 1~2문장
           (d) **원문 맥락에 맞는** 작고 구체적인 다음 주 제안 1~2개 (새 사실 창조 금지)
        7) 결과는 아래 **라벨 형식 그대로** 출력하고, 다른 문구나 코드블록/표시는 절대 넣지 말 것.

        [출력 형식(라벨 고정)]
        제목: (20자 이내, 이번 주 흐름을 함축. 추측어/과장 금지)
        피드백: (최소 6문장, 약 350~900자. 공감적이되 근거 참조 포함)

        [주간 일기 원문(한 주 전체가 합쳐진 문자열)]
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