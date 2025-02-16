package com.openclassrooms.ycyw.service;

import com.openclassrooms.ycyw.model.ChatMessage;
import com.openclassrooms.ycyw.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(final ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatMessage createChatMessage(ChatMessage chatMessage) {
        return this.chatMessageRepository.save(chatMessage);
    }
}
