package thedayoftoday.domain.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import thedayoftoday.entity.WeeklyData;
import thedayoftoday.repository.UserRepository;
import thedayoftoday.repository.WeeklyDataRepository;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import thedayoftoday.scheduler.WeeklySummaryScheduler;

@SpringBootTest
class WeeklySummarySchedulerManualTest {

    @Autowired
    private WeeklySummaryScheduler weeklySummaryScheduler;

    @Autowired
    private WeeklyDataRepository weeklyDataRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void summarize_ShouldCreateWeeklyData_ForPastWeek() {
        // given - DB에 5/5~5/7 일기가 있다고 가정

        // when - 스케줄러 수동 실행
        weeklySummaryScheduler.summarizeWeeklyDiaries();

        // then - WeeklyData 생성 확인
        List<WeeklyData> weeklyDataList = weeklyDataRepository.findAll();
        assertThat(weeklyDataList).isNotEmpty();

        WeeklyData data = weeklyDataList.get(0);
        System.out.println("📌 Title: " + data.getTitle());
        System.out.println("📌 Feedback: " + data.getFeedback());
        System.out.println("📌 Degree: " + data.getDegree());
        System.out.println("📌 Start Date: " + data.getStartDate());
        System.out.println("📌 End Date: " + data.getEndDate());

        // 검증: 날짜는 5월 5일(월 기준 시작) ~ 5월 11일(일 기준 종료)
        assertThat(data.getStartDate()).isEqualTo(LocalDate.of(2025, 5, 5));
        assertThat(data.getEndDate()).isEqualTo(LocalDate.of(2025, 5, 11));
    }
}