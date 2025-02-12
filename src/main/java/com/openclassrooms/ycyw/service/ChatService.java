package com.openclassrooms.ycyw.service;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    // store waiting usernames to allow retrieval
    private final HashSet<String> waitingUsers = new HashSet<>();

    /**
     * Returns the username associated with the http session
     * @param httpSessionId the http session identifier
     * @return an optional with the username if present
     */
    public Optional<String> getSessionUsername(String httpSessionId) {
        // use actual user authentication if available
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails) {
            return Optional.of(auth.getName());
        }

        String username = usernames.get(httpSessionId);
        if (username != null) {
            return Optional.of(username);
        }
        return Optional.empty();
    }

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
