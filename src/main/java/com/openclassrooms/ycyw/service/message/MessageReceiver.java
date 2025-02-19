package com.openclassrooms.ycyw.service.message;


import com.openclassrooms.ycyw.dto.ChatMessageDto;
import com.openclassrooms.ycyw.model.ChatMessageType;

import java.security.Principal;
import java.util.List;

/**
 * @author Wilhelm Zwertvaegher
 * Date:17/02/2025
 * Time:21:18
 */
public interface MessageReceiver {

    void receiveMessage(ChatMessageDto message, Principal principal);

    List<ChatMessageType> messageTypes();
}
