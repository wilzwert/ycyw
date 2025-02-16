package com.openclassrooms.ycyw.security.service;


import com.openclassrooms.ycyw.model.User;
import com.openclassrooms.ycyw.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService used to retrieve user by username extracted from a jwt token subject
 * As of now, the jwt token subject is the user's id
 *
 * @author Wilhelm Zwertvaegher
 * Date:02/16/2025
 * Time:15:58
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, NumberFormatException {
        User foundUser = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return org.springframework.security.core.userdetails.User.builder()
                .username(foundUser.getUsername())
                .password(foundUser.getPassword())
                .roles(foundUser.getRole())
                .build();
    }
}