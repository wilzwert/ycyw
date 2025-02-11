package com.openclassrooms.ycyw.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * @author Wilhelm Zwertvaegher
 * Date:06/02/2025
 * Time:10:13
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/support.html").hasRole("SUPPORT")
                        .requestMatchers("/js/support.js").hasRole("SUPPORT")
                        .anyRequest().permitAll() // Le reste est public
                )
                .formLogin(withDefaults()) // Form Login
                .logout(withDefaults()) // default logout
                .csrf(AbstractHttpConfigurer::disable) // deactivate CSRF for this POC
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }

    /**
     * Mock users
     * @return the UserDetailsService
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("support")
                .password("password")
                .roles("SUPPORT")
                .build();

        UserDetails user2 = User.withDefaultPasswordEncoder()
                .username("agent")
                .password("password")
                .roles("SUPPORT")
                .build();

        UserDetails user3 = User.withDefaultPasswordEncoder()
                .username("client")
                .password("password")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user, user2, user3);
    }
}