package com.openclassrooms.ycyw.configuration;


import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.http.WebSocketHandshakeException;

/**
 * @author Wilhelm Zwertvaegher
 * Date:14/02/2025
 * Time:09:04
 */

public class CustomWebSocketHandler extends TextWebSocketHandler {

    private WebSocketSession session;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println(session);
        this.session = session;
        super.afterConnectionEstablished(session);
    }

    public String getSessionId() {
        return session.getId();
    }
}
