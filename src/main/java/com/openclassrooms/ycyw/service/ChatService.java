package com.openclassrooms.ycyw.service;


import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Wilhelm Zwertvaegher
 * Date:10/02/2025
 * Time:12:41
 */

@Service
public class ChatService {
    private final HashMap<String, String> usernames = new HashMap<>();
    private final HashMap<String, String> sessions = new HashMap<>();
    private final HashMap<String, String> activeSessions = new HashMap<>();

    public String getUsername(String httpSessionId) {
        if(usernames.containsKey(httpSessionId)) {
            return usernames.get(httpSessionId);
        }

        // generate anonymous user and prevent collision with existing users
        int c = this.usernames.size()+1;
        String username = "";
        do {
            c++;
            username = "user"+c;
        }
        while (this.usernames.containsKey(username));

        this.usernames.put(httpSessionId, username);
        return username;
    }

    public boolean hasActiveSession(String username) {
        return activeSessions.containsKey(username);
    }

    /**
     * Memoize a simpSessionId for the username
     * @param username
     * @param simpSessionId
     */
    public void registerSimpSession(String username, String simpSessionId) {
        sessions.put(simpSessionId, username);
    }

    /**
     * Memoize the support agent's sessionId of the chat session opened for username
     * @param username
     * @param sessionId
     */
    public void setActiveSession(String username, String sessionId) {
        activeSessions.put(username, sessionId);
    }

    /**
     *
     * @param sessionId
     * @return a list of usernames handled by the support user corresponding to the sessionId
     */
    public List<String> getActiveSessions(String sessionId) {
        return activeSessions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(sessionId))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}
