package com.openclassrooms.ycyw.service.message;


import com.openclassrooms.ycyw.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Wilhelm Zwertvaegher
 * Date:17/02/2025
 * Time:22:49
 */
public interface SupportMessageReceiver extends MessageReceiver {
    @Autowired
    default void registerMe(ChatService service) {
        service.registerSupportMessageReceiver(messageTypes(), this);
    }
}
