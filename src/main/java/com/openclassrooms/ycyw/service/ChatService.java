package com.openclassrooms.ycyw.service;


import org.springframework.stereotype.Service;

import java.util.*;

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

    /**
     *
     * @param httpSessionId the http session id
     * @return a generated username or an existent one if http session id already has one
     */
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

    /**
     *
     * @param username the username of the user
     * @return true if username already handled by a support agent
     */
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

    /**
     * Adds a username to the list of users waiting to be handled by support
     * @param username the username of the chat user
     */
    public void addWaitingUser(String username) {
        waitingUsers.add(username);
    }

    /**
     * Removes a username to the list of users waiting to be handled by support
     * @param username the username of the chat user
     */
    public void removeWaitingUser(String username) {
        waitingUsers.remove(username);
    }

    /**
     *
     * @return the set of users waiting to be handled by support
     */
    public Set<String> getWaitingUsers() {
        return waitingUsers;
    }
}
