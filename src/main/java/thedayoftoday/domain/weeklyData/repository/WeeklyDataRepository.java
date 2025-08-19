package thedayoftoday.domain.weeklyData.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import thedayoftoday.domain.weeklyData.entity.WeeklyData;

@Repository
public interface WeeklyDataRepository extends JpaRepository<WeeklyData, Long> {

    Optional<WeeklyData> findByUser_UserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long userId, LocalDate endDate, LocalDate startDate
    );
}
