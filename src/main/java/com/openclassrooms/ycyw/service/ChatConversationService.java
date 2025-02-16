package com.openclassrooms.ycyw.service;

import com.openclassrooms.ycyw.model.ChatConversation;
import com.openclassrooms.ycyw.model.User;
import com.openclassrooms.ycyw.repository.ChatConversationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatConversationService {

    private final ChatConversationRepository chatConversationRepository;

    public ChatConversationService(final ChatConversationRepository chatConversationRepository) {
        this.chatConversationRepository = chatConversationRepository;
    }

    public ChatConversation createConversation(ChatConversation chatConversation) {
        return this.chatConversationRepository.save(chatConversation);
    }

    public Optional<ChatConversation> getConversationById(UUID conversationId) {
        return chatConversationRepository.findById(conversationId);
    }

    public void closeConversation(ChatConversation chatConversation) {
        chatConversation.setEndedAt(LocalDateTime.now());
        this.chatConversationRepository.save(chatConversation);
    }

    public ChatConversation setHandler(ChatConversation chatConversation, User user) {
        chatConversation.setHandler(user);
        return this.chatConversationRepository.save(chatConversation);
    }


}
