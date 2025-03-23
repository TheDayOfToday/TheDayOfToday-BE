package com.example.thedayoftoday.domain.repository;

import com.example.thedayoftoday.domain.entity.Conversation;
import com.example.thedayoftoday.domain.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findAllByDiaryOrderByConversationIdAsc(Diary diary);
}
