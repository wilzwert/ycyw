package com.openclassrooms.ycyw.service;


import com.openclassrooms.ycyw.dto.ChatMessageDto;
import com.openclassrooms.ycyw.dto.ChatUserDto;
import com.openclassrooms.ycyw.model.ChatMessageType;
import com.openclassrooms.ycyw.service.message.MessageReceiver;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Wilhelm Zwertvaegher
 * Date:17/02/2025
 * Time:21:10
 */
public interface ChatService {

    /**
     * Adds a username to the list of users waiting to be handled by support
     * @param user the chat user
     */
    void addWaitingUser(ChatUserDto user);

    /**
     * Removes a username to the list of users waiting to be handled by support
     * @param conversationId the id of the conversation
     */
    void removeWaitingUser(UUID conversationId);

    Optional<ChatUserDto> getWaitingUser(String username);

    /**
     *
     * @return the map of users waiting to be handled by support
     */
    Map<UUID, ChatUserDto> getWaitingUsers();


    void registerSupportMessageReceiver(List<ChatMessageType> types, MessageReceiver supportMessageReceiver);

    void registerPrivateMessageReceiver(List<ChatMessageType> types, MessageReceiver supportMessageReceiver);

    void receiveSupportMessage(ChatMessageDto message, Principal principal);

    void receivePrivateMessage(ChatMessageDto message, Principal principal);

}
