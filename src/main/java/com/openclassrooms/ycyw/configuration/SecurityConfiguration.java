package com.openclassrooms.ycyw.configuration;


import com.openclassrooms.ycyw.security.jwt.JwtAuthenticationFilter;
import com.openclassrooms.ycyw.security.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * @author Wilhelm Zwertvaegher
 * Date:06/02/2025
 * Time:10:13
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(CustomUserDetailsService customUserDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/support").hasRole("SUPPORT")
                        .requestMatchers("/js/support.js").hasRole("SUPPORT")
                        .anyRequest().permitAll() // Le reste est public
                )
                // .formLogin(withDefaults()) // Form Login
                // .logout((logout) -> logout.logoutSuccessUrl("/")) // logout redirects to home
                // disable CSRF protection, as the app is RESTful API / Websockets
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // insert our custom filter, which will authenticate user from token if provided in the request headers
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Mock users
     * @return the UserDetailsService
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return userDetailsService;
    }
}