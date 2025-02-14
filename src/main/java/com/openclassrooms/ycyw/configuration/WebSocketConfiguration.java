package com.openclassrooms.ycyw.configuration;


import com.openclassrooms.ycyw.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * @author Wilhelm Zwertvaegher
 * Date:05/02/2025
 * Time:15:52
 */

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer//, WebSocketConfigurer
{

    private final ChatService chatService;

    public WebSocketConfiguration(ChatService chatService) {
        super();
        this.chatService = chatService;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new CustomHandshakeHandler(chatService))
                ;
    }
}
