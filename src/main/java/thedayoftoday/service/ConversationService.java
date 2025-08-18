package thedayoftoday.service;

import thedayoftoday.entity.Conversation;
import thedayoftoday.entity.Diary;
import thedayoftoday.repository.ConversationRepository;
import thedayoftoday.repository.DiaryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public String mergeConversationText(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("다이어리가 존재하지 않습니다."));

        List<Conversation> conversations = conversationRepository.findAllByDiaryOrderByConversationIdAsc(diary);

        StringBuilder combinedText = new StringBuilder();
        for (Conversation conv : conversations) {
            combinedText.append("Q: ").append(conv.getQuestion()).append("\n");
            combinedText.append("A: ").append(conv.getAnswer()).append("\n\n");
        }

        return combinedText.toString().trim();
    }
}
