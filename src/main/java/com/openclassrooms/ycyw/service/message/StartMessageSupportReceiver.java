package com.openclassrooms.ycyw.service.message;


import com.openclassrooms.ycyw.dto.ChatMessageDto;
import com.openclassrooms.ycyw.dto.ChatUserDto;
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
public class StartMessageSupportReceiver implements SupportMessageReceiver {

    private final ChatService chatService;
    private final UserService userService;
    private final ChatConversationService chatConversationService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    StartMessageSupportReceiver(ChatService chatService, UserService userService, ChatConversationService chatConversationService, SimpMessagingTemplate simpMessagingTemplate) {
        this.chatService = chatService;
        this.userService = userService;
        this.chatConversationService = chatConversationService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }


    @Override
    public void receiveMessage(ChatMessageDto message, Principal principal) {
        // at first, we create a conversation
        // but if another START command arrives, we first check if the user is already waiting
        // in that case we reuse the previously created conversation
        Optional<ChatUserDto> userDtoOptional = this.chatService.getWaitingUser(principal.getName());
        if(userDtoOptional.isEmpty()) {

            Optional<User> foundUser = this.userService.findUserByUsername(principal.getName());
            ChatConversation chatConversation = new ChatConversation()
                    .setInitiatorUsername(principal.getName())
                    .setInitiator(foundUser.orElse(null))
                    ;
            ChatUserDto userDto = new ChatUserDto(principal.getName(), this.chatConversationService.createConversation(chatConversation).getId());

            // set user in waiting users list
            chatService.addWaitingUser(userDto);

            // broadcast start message to support
            ChatMessageDto result = new ChatMessageDto(principal.getName(), message.recipient(), ChatMessageType.START, "", userDto.conversationId());
            simpMessagingTemplate.convertAndSend("/topic/support", result);

            // send conversationId to initiator user
            ChatMessageDto senderResult = new ChatMessageDto(principal.getName(), principal.getName(), ChatMessageType.START, "", userDto.conversationId());
            simpMessagingTemplate.convertAndSendToUser(principal.getName(), "/queue/messages", senderResult);
        }
    }

    @Override
    public List<ChatMessageType> messageTypes() {
        return List.of(ChatMessageType.START);
    }
}
