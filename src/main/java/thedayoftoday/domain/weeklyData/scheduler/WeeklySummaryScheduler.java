package thedayoftoday.domain.weeklyData.scheduler;

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

@Service
@RequiredArgsConstructor
@Transactional
public class WeeklySummaryScheduler {

    private final WeeklyAnalysisService weeklyAnalysisService;
    private final AiService aiService;
    private final UserRepository userRepository;
    private final WeeklyDataRepository weeklyDataRepository;

    @Scheduled(cron = "59 59 23 * * SUN", zone = "Asia/Seoul")
    public void summarizeWeeklyDiaries() {
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            LocalDate now = LocalDate.now();
            LocalDate[] weekRange = weeklyAnalysisService.calculateStartAndEndDate(now);
            LocalDate startDate = weekRange[0];
            LocalDate endDate = weekRange[1];

            List<Diary> diaries = weeklyAnalysisService.extractedWeeklyDiaryData(user.getUserId(), weekRange);
            String combined = weeklyAnalysisService.combineWeeklyDiary(diaries);
            if (combined.isBlank()) {
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
        }
    }
}
