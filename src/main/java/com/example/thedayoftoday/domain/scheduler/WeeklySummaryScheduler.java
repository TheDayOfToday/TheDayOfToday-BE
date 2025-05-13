package com.example.thedayoftoday.domain.scheduler;

import com.example.thedayoftoday.domain.dto.weeklyAnalysis.WeeklyTitleFeedbackResponseDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.entity.WeeklyData;
import com.example.thedayoftoday.domain.entity.enumType.Degree;
import com.example.thedayoftoday.domain.repository.UserRepository;
import com.example.thedayoftoday.domain.repository.WeeklyDataRepository;
import com.example.thedayoftoday.domain.service.AiService;
import com.example.thedayoftoday.domain.service.WeeklyAnalysisService;
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

    @Scheduled(cron = "59 55 11 * * TUE", zone = "Asia/Seoul")
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
