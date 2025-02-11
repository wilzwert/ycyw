package com.openclassrooms.ycyw.controller;


import com.openclassrooms.ycyw.model.ChatMessage;
import com.openclassrooms.ycyw.model.ChatMessageType;
import com.openclassrooms.ycyw.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestContextHolder;

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

    private final ChatService chatService;


    public ChatController(SimpMessagingTemplate simpMessagingTemplate, SimpUserRegistry simpUserRegistry, ChatService chatService) {
        this.messagingTemplate = simpMessagingTemplate;
        this.userRegistry = simpUserRegistry;
        this.chatService = chatService;
    }

    /**
     * Start a chat session
     * @param message the message to send to support
     * @throws Exception
     */
    @MessageMapping("/support")
    public void sendSupportMessage(@Payload ChatMessage message, Principal principal, @Header("simpSessionId") String sessionId, StompHeaderAccessor accessor) throws Exception {
        System.out.println("User message");
        System.out.println(principal.getName());
        System.out.println(message);
        System.out.println(message.type());
        System.out.println(this.userRegistry.getUsers());
        System.out.println("SESSION "+sessionId);

        this.chatService.registerSimpSession(principal.getName(), sessionId);

        // if user already is handled, then we can send them a handle message
        if(message.type().equals(ChatMessageType.HANDLE)) {
            String httpSessionId = accessor.getSessionAttributes().get("sessionId").toString();
            // this chat session will be attached to the current user's http session
            // this allows us to have a unique user participating in different chats in different sessions
            this.chatService.setActiveSession(message.recipient(), httpSessionId);
            System.out.println("HTTP Session is "+httpSessionId);

            System.out.println("IS ALREADY HANDLED "+message.recipient());
            // TODO
            ChatMessage result = new ChatMessage(principal.getName(), message.recipient(), ChatMessageType.HANDLE, "");
            // let the user know
            messagingTemplate.convertAndSendToUser(message.recipient(), "/user/queue/messages", result);
            // let all support users know
            messagingTemplate.convertAndSend("/topic/support", result);

        }
        else if(message.type().equals(ChatMessageType.START)) {
            if(this.chatService.hasActiveSession(principal.getName())) {
                // let the user know
                ChatMessage result = new ChatMessage(message.recipient(), principal.getName(), ChatMessageType.HANDLE, "");
                messagingTemplate.convertAndSendToUser(principal.getName(), "/user/queue/messages", result);
            }
            else {
                System.out.println("LEt's broadcast start message");
                ChatMessage result = new ChatMessage(principal.getName(), message.recipient(), ChatMessageType.START, "");
                messagingTemplate.convertAndSend("/topic/support", result);
            }
        }
    }

    /**
     * Sends a
     * @param message the message to send to support
     * @throws Exception
     */
    /*
    @MessageMapping("/support")
    @SendTo("/topic/support")
    public ChatMessage sendSupportMessage(@Payload ChatMessage message, Principal principal) throws Exception {
        System.out.println("User message");
        System.out.println(principal.getName());
        System.out.println(message);
        System.out.println(message.type());
        System.out.println(this.userRegistry.getUsers());
        return new ChatMessage(principal.getName(), message.recipient(), message.type(), message.content());
    }*/

    /**
     * Sends a
     * @param message the handle message to send to support
     */
    @MessageMapping("/handle")
    @SendTo("/topic/support")
    public ChatMessage sendHandleMessage(@Payload ChatMessage message, Principal principal) {
        System.out.println("Handle message");
        System.out.println(principal.getName());
        System.out.println(message);
        System.out.println(message.type());
        System.out.println(this.userRegistry.getUsers());

        // this chat session will be attached to the current user's http session
        this.chatService.setActiveSession(message.recipient(), RequestContextHolder.currentRequestAttributes().getSessionId());

        // FIXME : it does not really make sense to consider that the user is the recipient
        return new ChatMessage(principal.getName(), message.recipient(), ChatMessageType.HANDLE, "");
    }

    @MessageMapping("/private")
    public void sendPrivateMessage(@Payload ChatMessage message, Principal principal) {
        System.out.println("Support message");
        System.out.println(message);
        if(message.recipient() != null) {
            System.out.println("send to "+message.recipient());
            ChatMessage result = new ChatMessage(principal.getName(), message.recipient(), message.type(), message.content());
            messagingTemplate.convertAndSendToUser(message.recipient(), "/user/queue/messages", result);
        }
    }
}
