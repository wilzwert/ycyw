package com.openclassrooms.ycyw.controller;

import com.openclassrooms.ycyw.dto.ChatMessageDto;
import com.openclassrooms.ycyw.model.ChatMessageType;
import com.openclassrooms.ycyw.service.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * @author Wilhelm Zwertvaegher
 * Date:05/02/2025
 * Time:15:52
 */

@Controller
@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Handle messages sent to the support
     * @param message the message sent to support
     * @param principal the user sending the message
     */
    @MessageMapping("/support")
    public void receiveSupportMessage(@Payload ChatMessageDto message, Principal principal) {
        this.chatService.receiveSupportMessage(message, principal);
    }

    /**
     * Receive a HANDLE message and broadcast it to ths support topic to inform that a username is now connected to a chat agent
     * @param message the handle message to send to support
     */
    @MessageMapping("/handle")
    @SendTo("/topic/support")
    public ChatMessageDto sendHandleMessage(@Payload ChatMessageDto message, Principal principal) {
        return new ChatMessageDto(principal.getName(), message.recipient(), ChatMessageType.HANDLE, "", message.conversationId());
    }

    /**
     * Receives a private message and forwards it to its recipient
     * This requires each user in the frontend to subscribe to a channel /user/queue/messages/[otheruser]
     * @param message the message to send
     * @param principal the user who is sending the message
     */
    @MessageMapping("/private")
    public void sendPrivateMessage(@Payload ChatMessageDto message, Principal principal) {
        this.chatService.receivePrivateMessage(message, principal);
    }
}