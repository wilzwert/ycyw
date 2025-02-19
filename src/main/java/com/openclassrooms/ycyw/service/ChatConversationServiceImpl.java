package com.openclassrooms.ycyw.service;

import com.openclassrooms.ycyw.dto.ChatMessageDto;
import com.openclassrooms.ycyw.model.ChatConversation;
import com.openclassrooms.ycyw.model.ChatMessage;
import com.openclassrooms.ycyw.model.User;
import com.openclassrooms.ycyw.repository.ChatConversationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatConversationServiceImpl implements ChatConversationService {

    private final ChatConversationRepository chatConversationRepository;

    private final ChatMessageService chatMessageService;

    public ChatConversationServiceImpl(final ChatConversationRepository chatConversationRepository, final ChatMessageService chatMessageService) {
        this.chatConversationRepository = chatConversationRepository;
        this.chatMessageService = chatMessageService;
    }

    @Override
    public ChatConversation createConversation(ChatConversation chatConversation) {
        return this.chatConversationRepository.save(chatConversation);
    }

    @Override
    public Optional<ChatConversation> getConversationById(UUID conversationId) {
        return chatConversationRepository.findById(conversationId);
    }

    @Override
    public void closeConversation(ChatConversation chatConversation) {
        chatConversation.setEndedAt(LocalDateTime.now());
        this.chatConversationRepository.save(chatConversation);
    }

    @Override
    public ChatConversation setHandler(ChatConversation chatConversation, User user) {
        chatConversation.setHandler(user);
        return this.chatConversationRepository.save(chatConversation);
    }


    public ChatMessage addMessageToConversation(ChatMessageDto messageDto) {
        Optional<ChatConversation> chatConversation = getConversationById(messageDto.conversationId());
        if(chatConversation.isPresent()) {
            ChatMessage newMessage = new ChatMessage()
                    .setConversation(chatConversation.get())
                    .setSender(messageDto.sender())
                    .setRecipient(messageDto.recipient())
                    .setContent(messageDto.content())
                    ;
            return this.chatMessageService.createChatMessage(newMessage);
        }
        return null;
    }
}
