package thedayoftoday.domain.diary.conversation.repository;

import thedayoftoday.domain.diary.conversation.entity.Conversation;
import thedayoftoday.domain.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findAllByDiaryOrderByConversationIdAsc(Diary diary);
}
