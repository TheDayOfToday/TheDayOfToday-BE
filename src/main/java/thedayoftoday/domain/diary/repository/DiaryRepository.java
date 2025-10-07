package thedayoftoday.domain.diary.repository;

import java.time.LocalDate;

import thedayoftoday.domain.diary.dto.DailyMoodColorDto;
import thedayoftoday.domain.diary.entity.Diary;
import thedayoftoday.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    boolean existsByUser_UserIdAndCreateTime(Long userId, LocalDate date);

    // 특정 유저 ID로 모든 Diary 조회
    List<Diary> findByUser_UserId(Long userId);

    @Query("SELECT d.user.name FROM Diary d WHERE d.diaryId = :diaryId")
    Optional<String> findUserNameByDiaryId(@Param("diaryId") Long diaryId);

    // 특정 유저 ID와 기간으로 Diary 조회
    List<Diary> findByUser_UserIdAndCreateTimeBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    // 특정 유저 ID로 모든 Diary 삭제
    void deleteByUser_UserId(Long userId);

    // 특정 유저 ID와 기간으로 Diary 삭제
    void deleteByUser_UserIdAndCreateTimeBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<Diary> findByDiaryId(Long diaryId);

    @Query("SELECT d FROM Diary d LEFT JOIN FETCH d.conversations WHERE d.diaryId = :diaryId")
    Optional<Diary> findByIdWithConversations(@Param("diaryId") Long diaryId);

    @Query("SELECT d FROM Diary d JOIN FETCH d.user WHERE d.user.userId = :userId AND d.title LIKE %:title%")
    List<Diary> findByUserIdAndTitleWithUser(@Param("userId") Long userId, @Param("title") String title);

    boolean existsByUserAndCreateTimeAfter(User user, LocalDate date);

    @Query("""
        SELECT new thedayoftoday.domain.diary.dto.DailyMoodColorDto(d.createTime, d.diaryMood.moodColor)
        FROM Diary d
        WHERE d.user.userId = :userId
        AND d.createTime BETWEEN :startDate AND :endDate
        AND d.diaryMood IS NOT NULL
    """)
    List<DailyMoodColorDto> findMoodColorsByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Optional<Diary> findTopByUser_UserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
    SELECT d
    FROM Diary d
    JOIN FETCH d.user u
    WHERE d.createTime BETWEEN :startDate AND :endDate
""")
    List<Diary> findAllByCreateTimeBetweenWithUser(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
