package com.openclassrooms.ycyw.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class UsernameGeneratorService {

    /**
     *
     * @return a generated username
     */
    public String generateUsername() {
        return UUID.randomUUID().toString();
    }
}
