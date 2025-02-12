package com.openclassrooms.ycyw.controller;


import com.openclassrooms.ycyw.model.ChatMessage;
import com.openclassrooms.ycyw.model.ChatMessageType;
import com.openclassrooms.ycyw.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

    private final SimpMessagingTemplate messagingTemplate;

    private final ChatService chatService;

    public ChatController(SimpMessagingTemplate simpMessagingTemplate, ChatService chatService) {
        this.messagingTemplate = simpMessagingTemplate;
        this.chatService = chatService;
    }

    /**
     * Handle messages sent to the /topic/support topic
     * @param message the message sent to support
     * @param principal the user sending the message
     */
    @MessageMapping("/support")
    public void sendSupportMessage(@Payload ChatMessage message, Principal principal) {

        ChatMessage result;
        switch (message.type()) {
            case HANDLE :
                result = new ChatMessage(principal.getName(), message.recipient(), ChatMessageType.HANDLE, "");
                // let the user know
                messagingTemplate.convertAndSendToUser(message.recipient(), "/user/queue/messages", result);
                // let all support users know
                messagingTemplate.convertAndSend("/topic/support", result);
                break;
            case START:
                // add username to waiting users list
                chatService.addWaitingUser(principal.getName());

                // broadcast start message to support
                result = new ChatMessage(principal.getName(), message.recipient(), ChatMessageType.START, "");
                messagingTemplate.convertAndSend("/topic/support", result);
                break;
            case QUIT:
                this.chatService.removeWaitingUser(principal.getName());
                result = new ChatMessage(principal.getName(), message.recipient(), ChatMessageType.QUIT, "");
                // let all support users know
                messagingTemplate.convertAndSend("/topic/support", result);
                break;
        }
    }

    /**
     * Broadcast a HANDLE message to support to inform that a username is now connected to a chat agent
     * @param message the handle message to send to support
     */
    @MessageMapping("/handle")
    @SendTo("/topic/support")
    public ChatMessage sendHandleMessage(@Payload ChatMessage message, Principal principal) {
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
}
