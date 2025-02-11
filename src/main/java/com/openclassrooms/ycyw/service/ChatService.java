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
    // map http session ids to generated usernames
    private final HashMap<String, String> usernames = new HashMap<>();
    // map usernames to support users http session id
    private final HashMap<String, String> activeSessions = new HashMap<>();
    // store waiting usernames to allow retrieval for support users
    private final HashSet<String> waitingUsers = new HashSet<>();

    public String getGeneratedUsername(String httpSessionId) {
        if(usernames.containsKey(httpSessionId)) {
            return usernames.get(httpSessionId);
        }

        // generate anonymous user and prevent collision with existing users
        int c = this.usernames.size()+1;
        String username;
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
     * Memoize the support agent's sessionId handling the chat session opened for username
     * @param username the user who is now connected to a support agent
     * @param httpSessionId the http session id of the support agent
     */
    public void setActiveSession(String username, String httpSessionId) {
        activeSessions.put(username, httpSessionId);
        removeWaitingUser(username);
    }

    public void addWaitingUser(String username) {
        waitingUsers.add(username);
    }

    public void removeWaitingUser(String username) {
        waitingUsers.remove(username);
    }

    public Set<String> getWaitingUsers() {
        return waitingUsers;
    }

    /**
     *
     * @param httpSessionId the http session id
     * @return a list of usernames handled by the support user corresponding to the sessionId
     */
    public List<String> getActiveSessions(String httpSessionId) {
        return activeSessions.values().stream()
                .filter(s -> s.equals(httpSessionId))
                .collect(Collectors.toList());
    }
}
