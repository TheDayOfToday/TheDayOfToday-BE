package com.example.thedayoftoday.domain.entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String nickname;
    private String name;
    private String email;
    private  String password;
    private String phoneNumber;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Diary> diaries = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Notice> notices = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WeeklyData> weeklyDataList = new ArrayList<>();

    @Builder
    public User(String nickname,
                String name,
                String email,
                String password,
                String phoneNumber,
                List<Diary> diaries,
                List<Notice> notices,
                List<WeeklyData> weeklyDataList) {
        this.nickname = nickname;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
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

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

}
