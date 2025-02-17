package com.openclassrooms.ycyw.service.message;


import com.openclassrooms.ycyw.dto.ChatMessageDto;
import com.openclassrooms.ycyw.model.ChatConversation;
import com.openclassrooms.ycyw.model.ChatMessageType;
import com.openclassrooms.ycyw.service.ChatConversationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * @author Wilhelm Zwertvaegher
 * Date:17/02/2025
 * Time:21:20
 */
@Component
public class CloseMessagePrivateReceiver extends DefaultMessagePrivateReceiver {

    private final ChatConversationService chatConversationService;

    CloseMessagePrivateReceiver(SimpMessagingTemplate simpMessagingTemplate, ChatConversationService chatConversationService) {
        super(simpMessagingTemplate);
        this.chatConversationService = chatConversationService;
    }


    @Override
    public void receiveMessage(ChatMessageDto message, Principal principal) {
        // end the conversation
        Optional<ChatConversation> chatConversation = this.chatConversationService.getConversationById(message.conversationId());
        chatConversation.ifPresent(this.chatConversationService::closeConversation);
        super.receiveMessage(message, principal);
    }

    @Override
    public List<ChatMessageType> messageTypes() {
        return List.of(ChatMessageType.CLOSE);
    }
}
