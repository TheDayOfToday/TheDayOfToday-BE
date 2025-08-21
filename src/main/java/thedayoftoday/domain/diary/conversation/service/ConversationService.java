package thedayoftoday.domain.diary.conversation.service;

import org.springframework.web.multipart.MultipartFile;
import thedayoftoday.domain.diary.conversation.dto.ConversationResponseDto;
import thedayoftoday.domain.diary.conversation.repository.ConversationRepository;
import thedayoftoday.domain.diary.conversation.entity.Conversation;
import thedayoftoday.domain.diary.entity.Diary;
import thedayoftoday.domain.diary.repository.DiaryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import thedayoftoday.domain.diary.service.AiService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final AiService aiService;

    @Transactional
    public void save(String question, String answer, Diary diary) {

        Conversation conversation = Conversation.builder()
                .question(question)
                .answer(answer)
                .diary(diary)
                .build();

        conversationRepository.save(conversation);
    }

    public String mergeConversationText(Diary diary) {

        List<Conversation> conversations = conversationRepository.findAllByDiaryOrderByConversationIdAsc(diary);

        StringBuilder combinedText = new StringBuilder();
        for (Conversation conv : conversations) {
            combinedText.append("Q: ").append(conv.getQuestion()).append("\n");
            combinedText.append("A: ").append(conv.getAnswer()).append("\n\n");
        }

        return combinedText.toString().trim();
    }

    @Transactional
    public ConversationResponseDto proccessAndGenerateNextQuestion(String question, MultipartFile audioFile, Diary diary) throws IOException {
        String answer = aiService.transcribeAudio(audioFile);

        save(question, answer, diary);

        String mergedText = mergeConversationText(diary);
        String nextQuestion = aiService.generateNextQuestion(mergedText);

        return new ConversationResponseDto(nextQuestion);
    }
}
