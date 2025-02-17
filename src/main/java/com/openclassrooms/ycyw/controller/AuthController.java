package com.openclassrooms.ycyw.controller;

import com.openclassrooms.ycyw.security.AuthenticationType;
import com.openclassrooms.ycyw.security.service.JwtService;
import com.openclassrooms.ycyw.dto.request.LoginRequestDto;
import com.openclassrooms.ycyw.dto.response.JwtResponse;
import com.openclassrooms.ycyw.service.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final UserServiceImpl userService;

    public AuthController(JwtService jwtService, UserServiceImpl userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JwtResponse login(@RequestBody(required = false)  LoginRequestDto loginRequestDto, Authentication authentication) {
        String role;

        // already authenticated ?
        UserDetails userDetails = (authentication != null ? (UserDetails) authentication : null);
        if(userDetails != null) {
            List<String> authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toCollection(ArrayList::new));

            // no login upgrade if current authentication is not anonymous
            if(null != loginRequestDto && !authorities.contains("ROLE_ANONYMOUS")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }

            if(null != loginRequestDto && authorities.contains("ROLE_ANONYMOUS")) {
                authentication = this.userService.authenticateUser(loginRequestDto.getUsername(), loginRequestDto.getPassword());
            }

            // generated new token and return the response
            role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().map(a -> a.replace("ROLE_", "")).orElse(null);
            String token = jwtService.generateToken(authentication.getName(), authorities.contains("ANONYMOUS") ? AuthenticationType.ANONYMOUS : AuthenticationType.USER);
            return new JwtResponse(token, "Bearer", authentication.getName(), role);
        }

        AuthenticationType authType = AuthenticationType.ANONYMOUS;
        // no pre-existing authentication, login request sent
        if(loginRequestDto != null) {
            authentication = this.userService.authenticateUser(loginRequestDto.getUsername(), loginRequestDto.getPassword());
            authType = AuthenticationType.USER;
            role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().map(a -> a.replace("ROLE_", "")).orElse(null);
        }
        else {
            authentication = this.userService.authenticateAnonymously();
            role = "ANONYMOUS";
        }

        log.info("User auth with authType {} and role {}", authType, role);
        String token = jwtService.generateToken(authentication.getName(), authType);
        return new JwtResponse(token, "Bearer", authentication.getName(), role);
    }
}