package com.openclassrooms.ycyw.configuration;

import org.springframework.context.event.EventListener;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.SimpAttributesContextHolder;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;


/**
 * @author Wilhelm Zwertvaegher
 * Date:14/02/2025
 * Time:08:51
 */
public class SessionIdHandshakeInterceptor implements HandshakeInterceptor {

    private SessionConnectedListener sessionConnectedListener;

    public SessionIdHandshakeInterceptor(SessionConnectedListener sessionConnectedListener) {
        this.sessionConnectedListener = sessionConnectedListener;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
        System.out.println(wsHandler.getClass());
        System.out.println("SessionIdHandshakeInterceptor afterHandshake called"+this.sessionConnectedListener.getSessionId());
        response.getHeaders().add("Age", this.sessionConnectedListener.getSessionId());
        System.out.println(response.getHeaders());
        if (wsHandler instanceof CustomWebSocketHandler) {
            // Récupérer la WebSocketSession
            CustomWebSocketHandler customHandler = (CustomWebSocketHandler) wsHandler;
            String sessionId = customHandler.getSessionId();  // Supposons que vous ayez une méthode pour obtenir la session
            // Ajouter l'ID de la session WebSocket dans les headers de la réponse
            response.getHeaders().add("X-Session-Id", sessionId);
        }
    }
}
