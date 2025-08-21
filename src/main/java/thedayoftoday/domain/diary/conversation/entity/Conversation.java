package thedayoftoday.domain.diary.conversation.entity;

import jakarta.persistence.*;
import lombok.*;
import thedayoftoday.domain.common.BaseEntity;
import thedayoftoday.domain.diary.entity.Diary;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Conversation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long conversationId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diaryId", nullable = false)
    private Diary diary;
}
