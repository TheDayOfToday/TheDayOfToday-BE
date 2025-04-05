package com.example.thedayoftoday.domain.dto.diary.conversation;

import org.springframework.web.multipart.MultipartFile;

public record ConversationRequestDto(String question, MultipartFile file) {
}
