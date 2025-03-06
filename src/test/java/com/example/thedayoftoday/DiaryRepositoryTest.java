package com.example.thedayoftoday;

import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.entity.SentimentalAnalysis;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.entity.enumType.MoodMeter;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class DiaryRepositoryTest {

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private TestEntityManager entityManager;

//    @Test
//    @DisplayName("특정 diary id로 감정 분석 조회")
//    void findSentimentAnalysisByDiaryId() {
//        // given
//        User user = User.builder()
//                .nickname("nickname")
//                .name("name")
//                .email("email@test.com")
//                .password("password")
//                .phoneNumber("010-1234-5678")
//                .build();
//
//        entityManager.persist(user);
//
//        SentimentalAnalysis analysis = SentimentalAnalysis.builder()
//                .moodName("Happy")
//                .moodmeter(MoodMeter.HAPPY)
//                .content("Feeling good!")
//                .build();
//        entityManager.persist(analysis);
//
//        Diary diary = Diary.builder()
//                .title("My Diary")
//                .content("This is the content.")
//                .createTime(LocalDateTime.now())
//                .user(user)
//                .sentimentAnalysis(analysis)
//                .build();
//        entityManager.persist(diary);
//
//        // when
//        Optional<SentimentalAnalysis> result = diaryRepository.findSentimentAnalysisByDiaryId(diary.getDiaryId());
//
//        // then
//        assertThat(result).isPresent();
//        assertThat(result.get().getMoodName()).isEqualTo("Happy");
//        assertThat(result.get().getContent()).isEqualTo("Feeling good!");
//    }

    @Test
    @DisplayName("특정 유저 ID로 모든 Diary 조회")
    void findByUser_UserId() {
        // given
        User user = User.builder()
                .nickname("nickname")
                .name("name")
                .email("email@test.com")
                .password("password")
                .phoneNumber("010-1234-5678")
                .build();
        entityManager.persist(user);

        Diary diary1 = Diary.builder()
                .title("Diary 1")
                .content("Content 1")
                .createTime(LocalDateTime.now())
                .user(user)
                .build();

        Diary diary2 = Diary.builder()
                .title("Diary 2")
                .content("Content 2")
                .createTime(LocalDateTime.now())
                .user(user)
                .build();

        entityManager.persist(diary1);
        entityManager.persist(diary2);

        // when
        List<Diary> diaries = diaryRepository.findByUser_UserId(user.getUserId());

        // then
        assertThat(diaries).hasSize(2);
        assertThat(diaries).extracting("title").contains("Diary 1", "Diary 2");
    }

    @Test
    @DisplayName("특정 유저 ID와 기간으로 Diary 조회")
    void findByUser_UserIdAndCreateTimeBetween() {
        // given
        User user = User.builder()
                .nickname("nickname")
                .name("name")
                .email("email@test.com")
                .password("password")
                .phoneNumber("010-1234-5678")
                .build();        entityManager.persist(user);

        LocalDateTime now = LocalDateTime.now();
        Diary diary1 = Diary.builder()
                .title("Diary 1")
                .content("Content 1")
                .createTime(now.minusDays(2))
                .user(user)
                .build();

        Diary diary2 = Diary.builder()
                .title("Diary 2")
                .content("Content 2")
                .createTime(now.minusDays(1))
                .user(user)
                .build();

        Diary diary3 = Diary.builder()
                .title("Diary 3")
                .content("Content 3")
                .createTime(now.plusDays(1))
                .user(user)
                .build();

        entityManager.persist(diary1);
        entityManager.persist(diary2);
        entityManager.persist(diary3);

        // when
        List<Diary> diaries = diaryRepository.findByUser_UserIdAndCreateTimeBetween(
                user.getUserId(),
                now.minusDays(3),
                now
        );

        // then
        assertThat(diaries).hasSize(2);
        assertThat(diaries).extracting("title").containsExactlyInAnyOrder("Diary 1", "Diary 2");
    }

    @Test
    @DisplayName("특정 유저 ID로 모든 Diary 삭제")
    void deleteByUser_UserId() {
        // given
        User user = User.builder()
                .nickname("nickname")
                .name("name")
                .email("email@test.com")
                .password("password")
                .phoneNumber("010-1234-5678")
                .build();
        entityManager.persist(user);

        Diary diary1 = Diary.builder()
                .title("Diary 1")
                .content("Content 1")
                .createTime(LocalDateTime.now())
                .user(user)
                .build();

        Diary diary2 = Diary.builder()
                .title("Diary 2")
                .content("Content 2")
                .createTime(LocalDateTime.now())
                .user(user)
                .build();

        entityManager.persist(diary1);
        entityManager.persist(diary2);

        // when
        diaryRepository.deleteByUser_UserId(user.getUserId());

        // then
        List<Diary> diaries = diaryRepository.findByUser_UserId(user.getUserId());
        assertThat(diaries).isEmpty();
    }

    @Test
    @DisplayName("특정 유저 ID와 기간으로 Diary 삭제")
    void deleteByUser_UserIdAndCreateTimeBetween() {
        // given
        User user = User.builder()
                .nickname("nickname")
                .name("name")
                .email("email@test.com")
                .password("password")
                .phoneNumber("010-1234-5678")
                .build();
        entityManager.persist(user);

        LocalDateTime now = LocalDateTime.now();
        Diary diary1 = Diary.builder()
                .title("Diary 1")
                .content("Content 1")
                .createTime(now.minusDays(2))
                .user(user)
                .build();

        Diary diary2 = Diary.builder()
                .title("Diary 2")
                .content("Content 2")
                .createTime(now.minusDays(1))
                .user(user)
                .build();

        Diary diary3 = Diary.builder()
                .title("Diary 3")
                .content("Content 3")
                .createTime(now.plusDays(1))
                .user(user)
                .build();

        entityManager.persist(diary1);
        entityManager.persist(diary2);
        entityManager.persist(diary3);

        // when
        diaryRepository.deleteByUser_UserIdAndCreateTimeBetween(
                user.getUserId(),
                now.minusDays(3),
                now
        );

        // then
        List<Diary> remainingDiaries = diaryRepository.findByUser_UserId(user.getUserId());
        assertThat(remainingDiaries).hasSize(1);
        assertThat(remainingDiaries.get(0).getTitle()).isEqualTo("Diary 3");
    }
}