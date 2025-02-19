package com.openclassrooms.ycyw.service.message;


import com.openclassrooms.ycyw.dto.ChatMessageDto;
import com.openclassrooms.ycyw.model.ChatConversation;
import com.openclassrooms.ycyw.model.ChatMessageType;
import com.openclassrooms.ycyw.model.User;
import com.openclassrooms.ycyw.service.ChatConversationService;
import com.openclassrooms.ycyw.service.ChatService;
import com.openclassrooms.ycyw.service.UserService;
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
public class HandleMessageSupportReceiver implements SupportMessageReceiver {

    private final ChatService chatService;
    private final UserService userService;
    private final ChatConversationService chatConversationService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    HandleMessageSupportReceiver(ChatService chatService, UserService userService, ChatConversationService chatConversationService, SimpMessagingTemplate simpMessagingTemplate) {
        this.chatService = chatService;
        this.userService = userService;
        this.chatConversationService = chatConversationService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }


    @Override
    public void receiveMessage(ChatMessageDto message, Principal principal) {
        Optional<User> user = this.userService.findUserByUsername(principal.getName());
        if(user.isPresent()) {
            Optional<ChatConversation> chatConversation = this.chatConversationService.getConversationById(message.conversationId());
            chatConversation.ifPresent(conversation -> this.chatConversationService.setHandler(conversation, user.get()));
        }

        this.chatService.removeWaitingUser(message.conversationId());
        ChatMessageDto result = new ChatMessageDto(principal.getName(), message.recipient(), ChatMessageType.HANDLE, "", message.conversationId());
        // let the user know
        simpMessagingTemplate.convertAndSendToUser(message.recipient(), "/queue/messages", result);
        // let all support users know
        simpMessagingTemplate.convertAndSend("/topic/support", result);
    }

    @Override
    public List<ChatMessageType> messageTypes() {
        return List.of(ChatMessageType.HANDLE);
    }
}
