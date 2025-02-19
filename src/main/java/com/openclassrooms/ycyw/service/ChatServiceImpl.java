package com.openclassrooms.ycyw.service;


import com.openclassrooms.ycyw.dto.ChatMessageDto;
import com.openclassrooms.ycyw.dto.ChatUserDto;
import com.openclassrooms.ycyw.exception.UnsupportedMessageTypeException;
import com.openclassrooms.ycyw.model.ChatMessageType;
import com.openclassrooms.ycyw.service.message.MessageReceiver;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;

/**
 * This service uses a strategy pattern to handle message received on the support topic
 * The implementation of the pattern is taken from step5 in this article : <a href="https://dzone.com/articles/spring-strategy-pattern">...</a>
 * @author Wilhelm Zwertvaegher
 * Date:10/02/2025
 * Time:12:41
 */

@Service
public class ChatServiceImpl implements ChatService {
    // store waiting usernames to allow retrieval
    private final HashMap<UUID, ChatUserDto> waitingUsers = new HashMap<>();

    private final Map<ChatMessageType, MessageReceiver> supportReceivers = new HashMap<>();
    private final Map<ChatMessageType, MessageReceiver> privateReceivers = new HashMap<>();

    @Override
    public void addWaitingUser(ChatUserDto user) {
        waitingUsers.put(user.conversationId(), user);
    }

    @Override
    public void removeWaitingUser(UUID conversationId) {
        waitingUsers.remove(conversationId);
    }

    @Override
    public Optional<ChatUserDto> getWaitingUser(String username) {
        return waitingUsers.entrySet()
                .stream()
                .filter((Map.Entry<UUID, ChatUserDto> s) -> s.getValue().username().equals(username))
                .findFirst()
                .map(Map.Entry::getValue);
    }


    @Override
    public Map<UUID, ChatUserDto> getWaitingUsers() {
        return waitingUsers;
    }

    @Override
    public void registerSupportMessageReceiver(List<ChatMessageType> types, MessageReceiver supportMessageReceiver) {
        types.forEach(type -> supportReceivers.put(type, supportMessageReceiver));
    }

    @Override
    public void registerPrivateMessageReceiver(List<ChatMessageType> types, MessageReceiver supportMessageReceiver) {
        types.forEach(type -> privateReceivers.put(type, supportMessageReceiver));
    }

    /**
     * @param message the message received
     * @param principal the current principal
     */
    @Override
    public void receiveSupportMessage(ChatMessageDto message, Principal principal) {
        MessageReceiver receiver = supportReceivers.get(message.type());
        if(receiver == null) {
            throw new UnsupportedMessageTypeException("Unsupported message type"+message.type());
        }
        receiver.receiveMessage(message, principal);
    }

    /**
     * @param message the message received
     * @param principal the current principal
     */
    @Override
    public void receivePrivateMessage(ChatMessageDto message, Principal principal) {
        MessageReceiver receiver = privateReceivers.get(message.type());
        if(receiver == null) {
            throw new UnsupportedMessageTypeException("Unsupported message type"+message.type());
        }
        receiver.receiveMessage(message, principal);
    }
}
