package com.openclassrooms.ycyw.controller;


import com.openclassrooms.ycyw.dto.ChatUserDto;
import com.openclassrooms.ycyw.dto.ChatMessageDto;
import com.openclassrooms.ycyw.model.ChatConversation;
import com.openclassrooms.ycyw.model.ChatMessage;
import com.openclassrooms.ycyw.model.ChatMessageType;
import com.openclassrooms.ycyw.model.User;
import com.openclassrooms.ycyw.service.ChatConversationService;
import com.openclassrooms.ycyw.service.ChatMessageService;
import com.openclassrooms.ycyw.service.ChatService;
import com.openclassrooms.ycyw.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Optional;

/**
 * @author Wilhelm Zwertvaegher
 * Date:05/02/2025
 * Time:15:52
 */

@Controller
@RestController
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    private final ChatService chatService;

    private final UserService userService;

    private final ChatConversationService chatConversationService;

    private final ChatMessageService chatMessageService;

    public ChatController(SimpMessagingTemplate simpMessagingTemplate, ChatService chatService, UserService userService, ChatConversationService chatConversationService, ChatMessageService chatMessageService) {
        this.messagingTemplate = simpMessagingTemplate;
        this.chatService = chatService;
        this.userService = userService;
        this.chatConversationService = chatConversationService;
        this.chatMessageService = chatMessageService;
    }

    /**
     * Handle messages sent to the /topic/support topic
     * @param message the message sent to support
     * @param principal the user sending the message
     */
    @MessageMapping("/support")
    public void sendSupportMessage(@Payload ChatMessageDto message, Principal principal) {

        ChatMessageDto result;
        // TODO implement strategy pattern to improve code readability and maintainability
        switch (message.type()) {
            case HANDLE :
                Optional<User> user = this.userService.findUserByUsername(principal.getName());
                if(user.isPresent()) {
                    Optional<ChatConversation> chatConversation = this.chatConversationService.getConversationById(message.conversationId());
                    chatConversation.ifPresent(conversation -> this.chatConversationService.setHandler(conversation, user.get()));
                }

                this.chatService.removeWaitingUser(message.conversationId());
                result = new ChatMessageDto(principal.getName(), message.recipient(), ChatMessageType.HANDLE, "", message.conversationId());
                // let the user know
                messagingTemplate.convertAndSendToUser(message.recipient(), "/queue/messages", result);
                // let all support users know
                messagingTemplate.convertAndSend("/topic/support", result);
                break;
            case START:
                // at first, we create a conversation
                // but if another START command arrives, we first check if the user is already waiting
                // in that case we reuse the previously created conversation
                Optional<ChatUserDto> userDtoOptional = this.chatService.getWaitingUser(principal.getName());
                if(userDtoOptional.isEmpty()) {
                    System.out.println(principal);
                    Optional<User> foundUser = this.userService.findUserByUsername(principal.getName());
                    ChatConversation chatConversation = new ChatConversation()
                            .setInitiatorUsername(principal.getName())
                            .setInitiator(foundUser.orElse(null))
                            ;
                    userDtoOptional = Optional.of(new ChatUserDto(principal.getName(), this.chatConversationService.createConversation(chatConversation).getId()));
                }

                ChatUserDto userDto = userDtoOptional.get();

                // set user in waiting users list
                chatService.addWaitingUser(userDto);

                // broadcast start message to support
                result = new ChatMessageDto(principal.getName(), message.recipient(), ChatMessageType.START, "", userDto.conversationId());
                messagingTemplate.convertAndSend("/topic/support", result);
                break;
            case QUIT:
                this.chatService.removeWaitingUser(message.conversationId());
                result = new ChatMessageDto(principal.getName(), message.recipient(), ChatMessageType.QUIT, "User has left the chat.", message.conversationId());
                // let all support users know
                messagingTemplate.convertAndSend("/topic/support", result);
                break;
            case CLOSE:
                // remove user from queue
                this.chatService.removeWaitingUser(message.conversationId());
                // TODO : when Conversation will be persisted, consider it ended
                // send CLOSE message to distant user
                result = new ChatMessageDto(principal.getName(), message.recipient(), ChatMessageType.CLOSE, "", message.conversationId());
                // let the user know
                messagingTemplate.convertAndSendToUser(message.recipient(), "/queue/messages/"+message.conversationId(), result);
                break;
        }
    }

    /**
     * Broadcast a HANDLE message to support to inform that a username is now connected to a chat agent
     * @param message the handle message to send to support
     */
    @MessageMapping("/handle")
    @SendTo("/topic/support")
    public ChatMessageDto sendHandleMessage(@Payload ChatMessageDto message, Principal principal) {
        return new ChatMessageDto(principal.getName(), message.recipient(), ChatMessageType.HANDLE, "", message.conversationId());
    }

    /**
     * Sends a private message
     * This requires each user in the frontend to subscribe to a channel /user/queue/messages/[otheruser]-user[socketSessionId]
     * @param message the message to send
     * @param principal the user who is sending the message
     */
    @MessageMapping("/private")
    public void sendPrivateMessage(@Payload ChatMessageDto message, Principal principal) {
        if(message.recipient() != null && message.type().equals(ChatMessageType.MESSAGE)) {
            Optional<ChatConversation> chatConversation = chatConversationService.getConversationById(message.conversationId());
            if(chatConversation.isPresent()) {
                ChatMessage newMessage = new ChatMessage()
                        .setConversation(chatConversation.get())
                        .setSender(message.sender())
                        .setRecipient(message.recipient())
                        .setContent(message.content())
                        ;
                this.chatMessageService.createChatMessage(newMessage);
            }
        }

        if(message.type().equals(ChatMessageType.CLOSE)) {
            // end the conversation
            Optional<ChatConversation> chatConversation = this.chatConversationService.getConversationById(message.conversationId());
            chatConversation.ifPresent(this.chatConversationService::closeConversation);
        }

        ChatMessageDto result = new ChatMessageDto(principal.getName(), message.recipient(), message.type(), message.content(), message.conversationId());
        messagingTemplate.convertAndSendToUser(message.recipient(), "/queue/messages/"+message.conversationId(), result);
    }
}
