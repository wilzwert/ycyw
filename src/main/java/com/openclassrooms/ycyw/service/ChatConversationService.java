package com.openclassrooms.ycyw.service;

import com.openclassrooms.ycyw.dto.ChatMessageDto;
import com.openclassrooms.ycyw.model.ChatConversation;
import com.openclassrooms.ycyw.model.ChatMessage;
import com.openclassrooms.ycyw.model.User;

import java.util.Optional;
import java.util.UUID;

public interface ChatConversationService {

    ChatConversation createConversation(ChatConversation chatConversation);

    Optional<ChatConversation> getConversationById(UUID conversationId);

    void closeConversation(ChatConversation chatConversation);

    ChatConversation setHandler(ChatConversation chatConversation, User user);

    ChatMessage addMessageToConversation(ChatMessageDto messageDto);
}
