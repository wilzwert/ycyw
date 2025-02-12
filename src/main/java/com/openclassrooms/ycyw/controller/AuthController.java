package com.openclassrooms.ycyw.controller;

import com.openclassrooms.ycyw.security.service.JwtService;
import com.openclassrooms.ycyw.dto.request.LoginRequestDto;
import com.openclassrooms.ycyw.dto.response.JwtResponse;
import com.openclassrooms.ycyw.service.UsernameGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Auth-related REST controller
 * @author Wilhelm Zwertvaegher
 * Date:07/11/2024
 * Time:15:51
 *
 */

@RestController
@Slf4j
@RequestMapping("/api/auth")

public class AuthController {
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UsernameGeneratorService usernameGeneratorService;

    public AuthController(UserDetailsService userDetailsService, JwtService jwtService, UsernameGeneratorService usernameGeneratorService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.usernameGeneratorService = usernameGeneratorService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JwtResponse login(@RequestBody(required = false)  LoginRequestDto loginRequestDto, Authentication authentication) {
        String username;
        String authType;
        boolean hasAnonymousRole = false;
        System.out.println(authentication);
        try {
            // authentication already exists
            // wa can upgrade an anonymous to a "regular" user but not the other way around
            if(authentication != null) {
                hasAnonymousRole = authentication.getAuthorities().stream()
                        .anyMatch(r -> r.getAuthority().equals("ROLE_ANONYMOUS"));
            }

            log.info("Authentication {}, hasAnonymousRole {}", authentication, hasAnonymousRole);

            if(loginRequestDto != null) {
                authType = "user";
                // login via username and password should not be used to authenticate if already authenticated
                if(authentication != null && !hasAnonymousRole) {
                    username = authentication.getName();
                }
                else {
                    log.info("User login with username {}", loginRequestDto.getUsername());
                    log.info("User login - authenticating");
                    UserDetails user = userDetailsService.loadUserByUsername(loginRequestDto.getUsername());
                    log.info("User login - generating token {} {}", user.getPassword(), loginRequestDto.getPassword());
                    if (!loginRequestDto.getPassword().equals(user.getPassword())) {
                        throw new BadCredentialsException("Wrong password");
                    }
                    username = user.getUsername();
                    log.info("User with username {} successfully authenticated, sending JWT token", loginRequestDto.getUsername());
                }
            }
            // no username/password and already authenticated, use current authentication
            else if(authentication != null) {
                username = authentication.getName();
                authType = hasAnonymousRole ? "anonymous" : "user";
            }
            // no username/password and not authenticated : generate anonoymous username
            else {
                authType = "anonymous";
                username = this.usernameGeneratorService.generateUsername();
            }
        }
        catch (AuthenticationException e) {
            log.info("Login failed for User with username {}", (loginRequestDto != null ? loginRequestDto.getUsername() : "none"));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed. " + e.getMessage());
        }

        log.info("Generate token for {}, authType {}", username, authType);
        String token = jwtService.generateToken(username, authType);
        return new JwtResponse(token, "Bearer", username);
    }
}