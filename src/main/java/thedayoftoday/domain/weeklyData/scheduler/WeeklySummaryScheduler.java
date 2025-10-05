package thedayoftoday.domain.weeklyData.scheduler;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import thedayoftoday.domain.weeklyData.dto.WeeklyTitleFeedbackResponseDto;
import thedayoftoday.domain.diary.entity.Diary;
import thedayoftoday.domain.user.entity.User;
import thedayoftoday.domain.weeklyData.entity.WeeklyData;
import thedayoftoday.domain.weeklyData.entity.Degree;
import thedayoftoday.domain.user.repository.UserRepository;
import thedayoftoday.domain.weeklyData.repository.WeeklyDataRepository;
import thedayoftoday.domain.diary.service.AiService;
import thedayoftoday.domain.weeklyData.service.WeeklyAnalysisService;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklySummaryScheduler {

    private final WeeklyAnalysisService weeklyAnalysisService;
    private final AiService aiService;
    private final UserRepository userRepository;
    private final WeeklyDataRepository weeklyDataRepository;

    @Scheduled(cron = "0 50 7 * * MON", zone = "Asia/Seoul")
    public void summarizeWeeklyDiaries() {
        log.info("[WEEKLY] 주간 분석 스케줄러 시작 시점: {}", LocalDateTime.now());

        List<User> allUsers = userRepository.findAll();
        LocalDate basis = LocalDate.now().minusDays(1);
        LocalDate[] weekRange = weeklyAnalysisService.calculateStartAndEndDate(basis);
        LocalDate startDate = weekRange[0];
        LocalDate endDate = weekRange[1];

        for (User user : allUsers) {
            try {
                List<Diary> diaries = weeklyAnalysisService.extractedWeeklyDiaryData(user.getUserId(), weekRange);
                String combined = weeklyAnalysisService.combineWeeklyDiary(diaries);

                if (combined.isBlank()) {
                    log.info("[WEEKLY] 사용자 {} — 이번 주({} ~ {}) 작성된 일기 없음, 분석 건너뜀",
                            user.getUserId(), startDate, endDate);
                    continue;
                }

                WeeklyTitleFeedbackResponseDto feedbackDto = aiService.analyzeWeeklyDiaryWithTitle(combined);
                Degree degree = aiService.analyzeDegree(combined);

                WeeklyData weeklyData = WeeklyData.builder()
                        .user(user)
                        .title(feedbackDto.title())
                        .feedback(feedbackDto.feedback())
                        .degree(degree)
                        .startDate(startDate)
                        .endDate(endDate)
                        .build();

                weeklyDataRepository.save(weeklyData);
                log.info("[WEEKLY] 사용자 {} — 주간 데이터 저장 완료 ({} ~ {})", user.getUserId(), startDate, endDate);

            } catch (Exception e) {
                log.warn("[WEEKLY] 사용자 {} — 주간 분석 중 오류 발생", user.getUserId(), e);
                log.warn("[WEEKLY] 상세 예외", e);
            }
        }
    }
}