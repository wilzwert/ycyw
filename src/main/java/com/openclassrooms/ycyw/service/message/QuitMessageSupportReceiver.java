package com.openclassrooms.ycyw.service.message;


import com.openclassrooms.ycyw.dto.ChatMessageDto;
import com.openclassrooms.ycyw.model.ChatMessageType;
import com.openclassrooms.ycyw.service.ChatService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

/**
 * @author Wilhelm Zwertvaegher
 * Date:17/02/2025
 * Time:21:20
 */
@Component
public class QuitMessageSupportReceiver implements SupportMessageReceiver {

    private final ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    QuitMessageSupportReceiver(ChatService chatService,SimpMessagingTemplate simpMessagingTemplate) {
        this.chatService = chatService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }


    @Override
    public void receiveMessage(ChatMessageDto message, Principal principal) {
        this.chatService.removeWaitingUser(message.conversationId());
        ChatMessageDto result = new ChatMessageDto(principal.getName(), message.recipient(), ChatMessageType.QUIT, "", message.conversationId());
        // let all support users know
        simpMessagingTemplate.convertAndSend("/topic/support", result);
    }

    @Override
    public List<ChatMessageType> messageTypes() {
        return List.of(ChatMessageType.QUIT);
    }
}
