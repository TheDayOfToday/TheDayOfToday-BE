package com.example.thedayoftoday.domain.repository;

import com.example.thedayoftoday.domain.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByDiary_DiaryId(Long diaryId);
}
