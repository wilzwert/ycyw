package com.openclassrooms.ycyw.controller;

import com.openclassrooms.ycyw.security.service.JwtService;
import com.openclassrooms.ycyw.dto.request.LoginRequestDto;
import com.openclassrooms.ycyw.dto.response.JwtResponse;
import com.openclassrooms.ycyw.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Auth-related REST controller
 * @author Wilhelm Zwertvaegher
 * Date:02/03/2025
 * Time:15:51
 *
 */

@RestController
@Slf4j
@RequestMapping("/api/auth")

public class AuthController {
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(JwtService jwtService,UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JwtResponse login(@RequestBody(required = false)  LoginRequestDto loginRequestDto, Authentication authentication) {
        String username;
        String authType;
        Collection<? extends GrantedAuthority> authorities = new ArrayList<>();
        boolean hasAnonymousRole = false;
        System.out.println(authentication);
        try {
            // authentication already exists
            // wa can upgrade an anonymous to a "regular" user but not the other way around
            if(authentication != null) {
                authorities = authentication.getAuthorities();
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
                    authentication = this.userService.authenticateUser(loginRequestDto.getUsername(), loginRequestDto.getPassword());
                    username = loginRequestDto.getUsername();
                    authorities = authentication.getAuthorities();
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
                username = this.userService.generateUsername();
            }
        }
        catch (AuthenticationException e) {
            log.info("Login failed for User with username {}", (loginRequestDto != null ? loginRequestDto.getUsername() : "none"));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed. " + e.getMessage());
        }


        String role = "ANONYMOUS";
        if(!authType.equals("anonymous")) {
            boolean hasSupportRole = authorities.stream()
                    .anyMatch(r -> r.getAuthority().equals("ROLE_SUPPORT"));
            if (hasSupportRole) {
                role = "SUPPORT";
            } else {
                role = "USER";
            }
        }

        log.info("Generate token for {}, authType {}", username, authType);
        String token = jwtService.generateToken(username, authType);
        return new JwtResponse(token, "Bearer", username, role);
    }
}