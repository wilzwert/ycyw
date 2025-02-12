package com.openclassrooms.ycyw.service;

import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class UsernameGeneratorService {
    private final Set<String> usernames = new HashSet<>();

    /**
     *
     * @return a generated username
     */
    public String generateUsername() {
        // generate anonymous user and prevent collision with existing users
        int c = this.usernames.size()+1;
        String username;
        do {
            c++;
            username = "user"+c;
        }
        while (this.usernames.contains(username));

        this.usernames.add(username);
        return username;
    }
}
