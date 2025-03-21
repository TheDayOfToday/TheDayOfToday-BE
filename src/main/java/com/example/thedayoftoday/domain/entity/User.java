package com.example.thedayoftoday.domain.entity;


import com.example.thedayoftoday.domain.entity.enumType.RoleType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    private String name;
    private String email;
    private String password;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private RoleType role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Diary> diaries = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Notice> notices = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WeeklyData> weeklyDataList = new ArrayList<>();

    @Builder
    public User(
            String name,
            String email,
            String password,
            String phoneNumber,
            RoleType role,
            List<Diary> diaries,
            List<Notice> notices,
            List<WeeklyData> weeklyDataList) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.diaries = diaries;
        this.notices = notices;
        this.weeklyDataList = weeklyDataList;
    }

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

}
