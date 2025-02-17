package com.openclassrooms.ycyw.service;

import com.openclassrooms.ycyw.model.ChatMessage;
import com.openclassrooms.ycyw.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageServiceImpl(final ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public ChatMessage createChatMessage(ChatMessage chatMessage) {
        return this.chatMessageRepository.save(chatMessage);
    }


}
