package com.openclassrooms.ycyw.configuration;


import com.openclassrooms.ycyw.service.ChatService;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpAttributesContextHolder;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.messaging.SessionConnectEvent;

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

    private SessionConnectedListener sessionConnectedListener;

    public WebSocketConfiguration(ChatService chatService, SessionConnectedListener sessionConnectedListener) {
        super();
        this.chatService = chatService;
        this.sessionConnectedListener = sessionConnectedListener;

    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new CustomHandshakeHandler(chatService))
                .addInterceptors(new SessionIdHandshakeInterceptor(this.sessionConnectedListener))
                // .withSockJS()
                ;
    }

    /*
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        System.out.println("registerWebSocketHandlers");
        registry.addHandler(new CustomWebSocketHandler(), "/ws")
                .addInterceptors(new SessionIdHandshakeInterceptor(this.sessionConnectedListener));
    }*/


    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        log.info("Registering client inbound channel");
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                System.out.println(message);
                System.out.println(message.getHeaders());
                System.out.println(message.getHeaders().getId());
                System.out.println(message.getHeaders().get("headers"));
                final StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
                final StompCommand command = headerAccessor.getCommand();
                System.out.println(command);

                /*
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);//message.

                if (StompCommand.CONNECTED.equals(accessor.getCommand())) {
                    log.info("Connect command received");
                    // Access authentication header(s) and invoke accessor.setUser(user)
                }*/
                return message;
            }
        });
    }
}
