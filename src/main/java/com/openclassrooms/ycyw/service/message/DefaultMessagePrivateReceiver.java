package com.openclassrooms.ycyw.service.message;


import com.openclassrooms.ycyw.dto.ChatMessageDto;
import com.openclassrooms.ycyw.model.ChatMessageType;
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
public class DefaultMessagePrivateReceiver implements PrivateMessageReceiver {

    protected final SimpMessagingTemplate simpMessagingTemplate;

    DefaultMessagePrivateReceiver(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }


    @Override
    public void receiveMessage(ChatMessageDto message, Principal principal) {
        ChatMessageDto result = new ChatMessageDto(principal.getName(), message.recipient(), message.type(), message.content(), message.conversationId());
        simpMessagingTemplate.convertAndSendToUser(message.recipient(), "/queue/messages/"+message.conversationId(), result);
    }

    @Override
    public List<ChatMessageType> messageTypes() {
        return List.of(
                ChatMessageType.TYPING,
                ChatMessageType.STOP_TYPING,
                ChatMessageType.QUIT,
                ChatMessageType.JOIN,
                ChatMessageType.PING,
                ChatMessageType.PING_RESPONSE
        );
    }
}
