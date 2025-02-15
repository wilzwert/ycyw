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
    // store waiting usernames to allow retrieval
    private final HashSet<String> waitingUsers = new HashSet<>();

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
