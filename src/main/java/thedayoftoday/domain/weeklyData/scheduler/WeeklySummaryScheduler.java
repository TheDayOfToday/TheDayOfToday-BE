package thedayoftoday.domain.weeklyData.scheduler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import thedayoftoday.domain.diary.repository.DiaryRepository;
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
@Transactional
@RequiredArgsConstructor
public class WeeklySummaryScheduler {

    private final WeeklyAnalysisService weeklyAnalysisService;
    private final AiService aiService;
    private final UserRepository userRepository;
    private final WeeklyDataRepository weeklyDataRepository;
    private final DiaryRepository diaryRepository;

    @Scheduled(cron = "0 00 00 * * MON", zone = "Asia/Seoul")
    public void summarizeWeeklyDiaries() {
        log.info("[WEEKLY] 주간 분석 스케줄러 시작 시점: {}", LocalDateTime.now());

        LocalDate basis = LocalDate.now().minusDays(1);
        LocalDate[] weekRange = weeklyAnalysisService.calculateStartAndEndDate(basis);
        LocalDate startDate = weekRange[0];
        LocalDate endDate = weekRange[1];

        List<Diary> allDiaries = diaryRepository.findAllByCreateTimeBetweenWithUser(startDate, endDate);
        if (allDiaries.isEmpty()) {
            log.info("[WEEKLY] 이번 주({} ~ {}) 작성된 일기가 한 건도 없음", startDate, endDate);
            return;
        }

        Map<Long, List<Diary>> extractDiariesWithUser = allDiaries.stream()
                .collect(Collectors.groupingBy(d -> d.getUser().getUserId()));

        for (Map.Entry<Long, List<Diary>> entry : extractDiariesWithUser.entrySet()) {
            Long userId = entry.getKey();
            List<Diary> diaries = entry.getValue();

            try {
                String combined = weeklyAnalysisService.combineWeeklyDiary(diaries);
                if (combined.isBlank()) {
                    log.info("[WEEKLY] 사용자 {} — 이번 주({} ~ {}) 작성된 일기 없음, 건너뜀",
                            userId, startDate, endDate);
                    continue;
                }

                WeeklyTitleFeedbackResponseDto feedbackDto = aiService.analyzeWeeklyDiaryWithTitle(combined);
                Degree degree = aiService.analyzeDegree(combined);

                WeeklyData weeklyData = WeeklyData.builder()
                        .title(feedbackDto.title())
                        .feedback(feedbackDto.feedback())
                        .degree(degree)
                        .startDate(startDate)
                        .endDate(endDate)
                        .build();

                User user = userRepository.getReferenceById(userId);
                weeklyData.setUser(user);
                weeklyDataRepository.save(weeklyData);
                log.info("[WEEKLY] 사용자 {} — 주간 데이터 저장 완료 ({} ~ {})",
                        userId, startDate, endDate);

            } catch (Exception e) {
                log.warn("[WEEKLY][ERROR] userId={} 기간:{}~{} type={} msg={}",
                        userId, startDate, endDate,
                        e.getClass().getSimpleName(), e.getMessage());
                log.debug("[WEEKLY][STACKTRACE]", e);
            }
        }
    }
}