package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.entity.Conversation;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.repository.ConversationRepository;
import com.example.thedayoftoday.domain.repository.DiaryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final DiaryRepository diaryRepository;

    @Transactional
    public void save(String question, String answer, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 다이어리가 존재하지 않습니다."));

        Conversation conversation = Conversation.builder()
                .question(question)
                .answer(answer)
                .diary(diary)
                .build();

        conversationRepository.save(conversation);
    }
}