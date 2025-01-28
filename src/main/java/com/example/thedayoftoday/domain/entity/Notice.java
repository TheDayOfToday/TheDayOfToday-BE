package com.example.thedayoftoday.domain.entity;

import com.example.thedayoftoday.domain.entity.enumType.NoticeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    @Enumerated(EnumType.STRING)
    private NoticeType noticeType;

    @Lob
    private String noticeContent;

    private LocalDateTime noticeTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Builder
    public Notice(NoticeType noticeType, String noticeContent, LocalDateTime noticeTime, User user) {
        this.noticeType = noticeType;
        this.noticeContent = noticeContent;
        this.noticeTime = noticeTime;
        this.user = user;
    }

}
