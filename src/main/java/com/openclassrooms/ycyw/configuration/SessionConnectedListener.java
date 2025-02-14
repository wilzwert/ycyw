package com.openclassrooms.ycyw.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpAttributesContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

/**
 * @author Wilhelm Zwertvaegher
 * Date:14/02/2025
 * Time:09:20
 */

@Component
public class SessionConnectedListener {
    String sessionId;

    @EventListener
    private void handleSessionConnected(SessionConnectEvent event) {
        System.out.println("SessionIdHandshakeInterceptor handleSessionConnected");
        this.sessionId = SimpAttributesContextHolder.currentAttributes().getSessionId();
        System.out.println("sessionId: " + sessionId);
    }

    public String getSessionId() {
        return sessionId;
    }
}
