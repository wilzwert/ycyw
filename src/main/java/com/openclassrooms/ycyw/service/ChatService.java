package com.openclassrooms.ycyw.service;


import com.openclassrooms.ycyw.dto.ChatUserDto;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Wilhelm Zwertvaegher
 * Date:10/02/2025
 * Time:12:41
 */

@Service
public class ChatService {
    // store waiting usernames to allow retrieval
    private final HashMap<UUID, ChatUserDto> waitingUsers = new HashMap<>();

    /**
     * Adds a username to the list of users waiting to be handled by support
     * @param user the chat user
     */
    public void addWaitingUser(ChatUserDto user) {
        waitingUsers.put(user.conversationId(), user);
    }

    /**
     * Removes a username to the list of users waiting to be handled by support
     * @param conversationId the id of the conversation
     */
    public void removeWaitingUser(UUID conversationId) {
        waitingUsers.remove(conversationId);
    }

    public Optional<ChatUserDto> getWaitingUser(String username) {
        return waitingUsers.entrySet()
                .stream()
                .filter((Map.Entry<UUID, ChatUserDto> s) -> s.getValue().username().equals(username))
                .findFirst()
                .map(Map.Entry::getValue);
    }

    /**
     *
     * @return the map of users waiting to be handled by support
     */
    public Map<UUID, ChatUserDto> getWaitingUsers() {
        return waitingUsers;
    }

    public UUID createConversation() {
        return UUID.randomUUID();
    }
}
