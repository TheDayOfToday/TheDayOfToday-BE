package com.example.thedayoftoday.domain.entity;

import com.example.thedayoftoday.domain.entity.enumType.Moodmeter;
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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long weeklyDataId;

    private String title;

    @Enumerated(EnumType.STRING)
    private Moodmeter analysisMoodmeter;

    @Lob
    private String feedback;

    private LocalDate startDate;

    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Builder
    public WeeklyData (String title,
                                 String feedback,
                                 LocalDate startDate,
                                 LocalDate endDate,
                                 User user) {
        this.title = title;
        this.feedback = feedback;
        this.startDate = startDate;
        this.endDate = endDate;
        this.user = user;
    }

}
