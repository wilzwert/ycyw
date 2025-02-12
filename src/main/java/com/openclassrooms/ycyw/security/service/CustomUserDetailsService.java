package com.openclassrooms.ycyw.security.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService used to retrieve user by username extracted from a jwt token subject
 * As of now, the jwt token subject is the user's id
 *
 * @author Wilhelm Zwertvaegher
 * Date:07/11/2024
 * Time:15:58
 */
@Service
@Slf4j
public class CustomUserDetailsService extends InMemoryUserDetailsManager {

    public CustomUserDetailsService() {
        super();

        UserDetails user = User.builder()
                .username("support")
                .password("password")
                .roles("SUPPORT")
                .build();

        UserDetails user2 = User.builder()
                .username("agent")
                .password("password")
                .roles("SUPPORT")
                .build();

        UserDetails user3 = User.builder()
                .username("client")
                .password("password")
                .roles("USER")
                .build();
        super.createUser(user);
        super.createUser(user2);
        super.createUser(user3);
    }
}