package com.example.thedayoftoday.domain.repository;

import com.example.thedayoftoday.domain.entity.Diary;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    // 특정 유저 ID로 모든 Diary 조회
    List<Diary> findByUser_UserId(Long userId);

    @Query("SELECT d.user.name FROM Diary d WHERE d.diaryId = :diaryId")
    Optional<String> findUserNameByDiaryId(@Param("diaryId") Long diaryId);

    // 특정 유저 ID와 기간으로 Diary 조회
    List<Diary> findByUser_UserIdAndCreateTimeBetween(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // 특정 유저 ID로 모든 Diary 삭제
    void deleteByUser_UserId(Long userId);

    // 특정 유저 ID와 기간으로 Diary 삭제
    void deleteByUser_UserIdAndCreateTimeBetween(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    Optional<Diary> findByDiaryId(Long diaryId);

    @Query("SELECT d FROM Diary d JOIN FETCH d.user WHERE d.user.userId = :userId AND d.title LIKE %:title%")
    List<Diary> findByUserIdAndTitleWithUser(@Param("userId") Long userId, @Param("title") String title);
}