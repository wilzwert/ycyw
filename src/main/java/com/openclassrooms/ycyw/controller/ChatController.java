package com.openclassrooms.ycyw.controller;


import com.openclassrooms.ycyw.model.ChatMessage;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * @author Wilhelm Zwertvaegher
 * Date:05/02/2025
 * Time:15:52
 */

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    private final SimpUserRegistry userRegistry;


    public ChatController(SimpMessagingTemplate simpMessagingTemplate, SimpUserRegistry simpUserRegistry) {
        this.messagingTemplate = simpMessagingTemplate;
        this.userRegistry = simpUserRegistry;
    }

    /**
     * Sends a
     * @param message the message to send to support
     * @throws Exception
     */
    @MessageMapping("/support")
    @SendTo("/topic/support")
    public ChatMessage start(@Payload ChatMessage message, Principal principal) throws Exception {
        System.out.println("User message");
        System.out.println(principal.getName());
        System.out.println(message);
        System.out.println(this.userRegistry.getUsers());
        return new ChatMessage(principal.getName(), message.recipient(), message.content());
    }

    @MessageMapping("/private")
    public void handleSupportMessage(@Payload ChatMessage message) throws Exception {
        System.out.println("Support message");
        System.out.println(message);
        if(message.recipient() != null && !message.recipient().equals("support")) {
            System.out.println("send to "+message.recipient());
            messagingTemplate.convertAndSendToUser(message.recipient(), "/user/queue/messages", message);
        }
    }
}
