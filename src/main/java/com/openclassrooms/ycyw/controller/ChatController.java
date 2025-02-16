package com.openclassrooms.ycyw.controller;


import com.openclassrooms.ycyw.dto.ChatUserDto;
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
        // TODO implement strategy pattern to improve code readability and maintainability
        switch (message.type()) {
            case HANDLE :
                this.chatService.removeWaitingUser(message.conversationId());
                result = new ChatMessage(principal.getName(), message.recipient(), ChatMessageType.HANDLE, "", message.conversationId());
                // let the user know
                messagingTemplate.convertAndSendToUser(message.recipient(), "/queue/messages", result);
                // let all support users know
                messagingTemplate.convertAndSend("/topic/support", result);
                break;
            case START:
                // at first, we create a conversation
                // but if another START command arrives, we first check if the user is already waiting
                // in that case we reuse the previously created conversation
                // TODO : conversation should be persisted
                // we should persist the conversation on START command even if Conversations not handled may be "empty"
                // but in the long term it may actually be helpful to identify empty conversations and when they occur
                Optional<ChatUserDto> userDtoOptional = this.chatService.getWaitingUser(principal.getName())
                        .or(() -> Optional.of(new ChatUserDto(principal.getName(), this.chatService.createConversation())));

                ChatUserDto userDto = userDtoOptional.get();

                System.out.println("User sent START");
                // add username to waiting users list
                System.out.println(principal);
                chatService.addWaitingUser(userDto);

                // broadcast start message to support
                result = new ChatMessage(principal.getName(), message.recipient(), ChatMessageType.START, "", userDto.conversationId());
                messagingTemplate.convertAndSend("/topic/support", result);

                break;
            case QUIT:
                this.chatService.removeWaitingUser(message.conversationId());
                result = new ChatMessage(principal.getName(), message.recipient(), ChatMessageType.QUIT, "User has left the chat.", message.conversationId());
                // let all support users know
                messagingTemplate.convertAndSend("/topic/support", result);
                break;
            case CLOSE:
                // remove user from queue
                this.chatService.removeWaitingUser(message.conversationId());
                // TODO : when Conversation will be persisted, consider it ended
                // send CLOSE message to distant user
                result = new ChatMessage(principal.getName(), message.recipient(), ChatMessageType.CLOSE, "", message.conversationId());
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
    public ChatMessage sendHandleMessage(@Payload ChatMessage message, Principal principal) {
        return new ChatMessage(principal.getName(), message.recipient(), ChatMessageType.HANDLE, "", message.conversationId());
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
            ChatMessage result = new ChatMessage(principal.getName(), message.recipient(), message.type(), message.content(), message.conversationId());
            messagingTemplate.convertAndSendToUser(message.recipient(), "/queue/messages/"+message.conversationId(), result);
        }
    }
}
