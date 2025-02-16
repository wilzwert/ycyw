package com.openclassrooms.ycyw.service;

import com.openclassrooms.ycyw.model.User;
import com.openclassrooms.ycyw.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    public Optional<User> findUserByUsername(final String username) {
        return userRepository.findByUsername(username);
    }

    public Authentication authenticateUser(String username, String password) throws AuthenticationException {
        Optional<User> user = findUserByUsername(username);
        if(user.isEmpty()) {
            log.info("User not found");
            throw new AuthenticationException("User not found") {
                @Override
                public String getMessage() {
                    return super.getMessage();
                }
            };
        }
        log.info("User has been found, let's pass to authenticationManager");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.get().getUsername(), password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }


    /**
     *
     * @return a generated username
     */
    public String generateUsername() {
        return UUID.randomUUID().toString();
    }
}
