package com.openclassrooms.ycyw.configuration;


import com.openclassrooms.ycyw.service.ChatService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * @author Wilhelm Zwertvaegher
 * Date:06/02/2025
 * Time:23:07
 */
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    private final ChatService chatService;

    public CustomHandshakeHandler(final ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        // use actual user authentication if available
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails) {
            return auth;
        }

        ServletServerHttpRequest httpRequest = (ServletServerHttpRequest) request;
        String sessionId = httpRequest.getServletRequest().getSession().getId();
        String username = this.chatService.getGeneratedUsername(sessionId);
        return () -> username;
    }
}
