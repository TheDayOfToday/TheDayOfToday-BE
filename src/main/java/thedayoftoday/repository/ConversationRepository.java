package thedayoftoday.repository;

import thedayoftoday.entity.Conversation;
import thedayoftoday.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findAllByDiaryOrderByConversationIdAsc(Diary diary);
}
