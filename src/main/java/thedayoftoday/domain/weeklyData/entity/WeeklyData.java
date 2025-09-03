package thedayoftoday.domain.weeklyData.entity;

import thedayoftoday.domain.common.BaseEntity;
import thedayoftoday.domain.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "weekly_data")
public class WeeklyData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long weeklyDataId;

    private String title;

    @Enumerated(EnumType.STRING)
    private Degree degree;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String feedback;

    private LocalDate startDate;

    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Builder
    public WeeklyData(String title,
                      Degree degree,
                      String feedback,
                      LocalDate startDate,
                      LocalDate endDate,
                      User user) {
        this.degree = degree;
        this.title = title;
        this.feedback = feedback;
        this.startDate = startDate;
        this.endDate = endDate;
        this.user = user;
    }
}
