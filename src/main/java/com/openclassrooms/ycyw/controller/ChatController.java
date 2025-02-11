package com.openclassrooms.ycyw.controller;


import com.openclassrooms.ycyw.model.ChatMessage;
import com.openclassrooms.ycyw.model.ChatMessageType;
import com.openclassrooms.ycyw.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import java.security.Principal;
import java.util.List;
import java.util.Objects;

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

    private final SimpUserRegistry userRegistry;


    public ChatController(SimpMessagingTemplate simpMessagingTemplate, ChatService chatService, SimpUserRegistry simpUserRegistry) {
        this.messagingTemplate = simpMessagingTemplate;
        this.chatService = chatService;
        this.userRegistry = simpUserRegistry;
    }

    /**
     * Hand messages sent to the /topic/support topic
     * @param message the message sent to support
     * @param principal the user sending the message
     * @param accessor an accessor used to retrieve
     *
     */
    @MessageMapping("/support")
    public void sendSupportMessage(@Payload ChatMessage message, Principal principal, StompHeaderAccessor accessor) {

        // if user gets handled, then we can send them a handle message
        if(message.type().equals(ChatMessageType.HANDLE)) {
            Object httpSessionIdObject = Objects.requireNonNull(accessor.getSessionAttributes()).get("sessionId");
            String httpSessionId = (String) httpSessionIdObject;
            // this chat session will be attached to the current support user's http session
            // this allows us to have a unique user participating in different chats in different sessions
            this.chatService.setActiveSession(message.recipient(), httpSessionId);

            ChatMessage result = new ChatMessage(principal.getName(), message.recipient(), ChatMessageType.HANDLE, "");
            // let the user know
            messagingTemplate.convertAndSendToUser(message.recipient(), "/user/queue/messages", result);
            // let all support users know
            messagingTemplate.convertAndSend("/topic/support", result);

        }
        else if(message.type().equals(ChatMessageType.START)) {
            if(this.chatService.hasActiveSession(principal.getName())) {
                // let the user know they're already handled
                ChatMessage result = new ChatMessage(message.recipient(), principal.getName(), ChatMessageType.HANDLE, "");
                messagingTemplate.convertAndSendToUser(principal.getName(), "/user/queue/messages", result);
            }
            else {
                System.out.println("Add waiting "+principal.getName());

                // add username to waiting users list
                chatService.addWaitingUser(principal.getName());

                // broadcast start message to support
                ChatMessage result = new ChatMessage(principal.getName(), message.recipient(), ChatMessageType.START, "");
                messagingTemplate.convertAndSend("/topic/support", result);
            }
        }
    }

    /**
     * Broadcast a HANDLE message to support to inform that a username is now connected to a chat agent
     * @param message the handle message to send to support
     */
    @MessageMapping("/handle")
    @SendTo("/topic/support")
    public ChatMessage sendHandleMessage(@Payload ChatMessage message, Principal principal) {
        // this chat session will be attached to the current user's http session
        this.chatService.setActiveSession(message.recipient(), RequestContextHolder.currentRequestAttributes().getSessionId());

        // FIXME : it does not really make sense to consider that the user is the recipient
        return new ChatMessage(principal.getName(), message.recipient(), ChatMessageType.HANDLE, "");
    }

    /**
     * Sends a private message
     * This requires each user in the frontend to subscribe to a channel /user/queue/messages/[otheruser]-user[socketSessionId]
     * @param message the message to send
     * @param principal the user who is sending the message
     */
    @MessageMapping("/private")
    public void sendPrivateMessage(@Payload ChatMessage message, Principal principal) {
        if(message.recipient() != null) {
            ChatMessage result = new ChatMessage(principal.getName(), message.recipient(), message.type(), message.content());
            messagingTemplate.convertAndSendToUser(message.recipient(), "/user/queue/messages/"+principal.getName(), result);
        }
    }

    /**
     * Get a list of users
     * @param filter a string defining a filter on the users list
     * @return a list of users using the chat service
     */
    @GetMapping(value= "/api/chat/users", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SUPPORT')")
    public List<String> getUsers(@RequestParam(required = false, defaultValue = "", value="filter") String filter) {
        if(filter.equals("waiting")) {
            return this.chatService.getWaitingUsers().stream().toList();
        }

        return this.userRegistry.getUsers().stream().map(SimpUser::getName).toList();
    }
}
