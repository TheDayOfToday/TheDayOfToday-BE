package thedayoftoday.domain.user.entity;

import org.springframework.security.crypto.password.PasswordEncoder;
import thedayoftoday.domain.auth.dto.SignupRequestDto;
import thedayoftoday.domain.book.entity.Book;
import thedayoftoday.domain.common.BaseEntity;
import thedayoftoday.domain.diary.entity.Diary;
import thedayoftoday.domain.notice.entity.Notice;
import thedayoftoday.domain.weeklyData.entity.WeeklyData;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;
    private String email;
    private String password;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private RoleType role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Diary> diaries = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Notice> notices = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WeeklyData> weeklyDataList = new ArrayList<>();

    public static User createUser(SignupRequestDto request, PasswordEncoder passwordEncoder) {
        return User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .role(RoleType.USER)
                .build();
    }

    public void addDiary(Diary diary) {
        this.diaries.add(diary);
        diary.setUser(this);
    }

    public void addNotice(Notice notice) {
        this.notices.add(notice);
        notice.setUser(this);
    }

    public void addWeeklyData(WeeklyData weeklyData) {
        this.weeklyDataList.add(weeklyData);
        weeklyData.linkUser(this);
    }

    public void updatePassword(String newPassword, PasswordEncoder passwordEncoder) {
        if (passwordEncoder.matches(newPassword, this.password)) {
            throw new IllegalArgumentException("기존의 비밀번호와 동일합니다.");
        }
        this.password = passwordEncoder.encode(newPassword);
    }
}
