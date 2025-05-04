package com.example.thedayoftoday.domain.entity;

import com.example.thedayoftoday.domain.entity.enumType.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;
    private String email;
    private String password;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private RoleType role;

    @Embedded
    private Book recommendedBook;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Diary> diaries = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Notice> notices = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WeeklyData> weeklyDataList = new ArrayList<>();

    public void addDiary(Diary diary) {
        diaries.add(diary);
    }

    public void addNotice(Notice notice) {
        notices.add(notice);
    }

    public void addWeeklyData(WeeklyData weeklyData) {
        weeklyDataList.add(weeklyData);
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void changeRecommendedBook(Book book) {
        this.recommendedBook = book;
    }
}
