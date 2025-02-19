package com.openclassrooms.ycyw.controller;

import com.openclassrooms.ycyw.dto.controller.AuthenticationTokenDto;
import com.openclassrooms.ycyw.dto.request.LoginRequestDto;
import com.openclassrooms.ycyw.dto.response.JwtResponse;
import com.openclassrooms.ycyw.service.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final UserServiceImpl userService;

    public AuthController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JwtResponse login(@RequestBody(required = false)  LoginRequestDto loginRequestDto, Authentication authentication) {
        AuthenticationTokenDto authenticationTokenDto = this.userService.login(authentication, loginRequestDto);
        String role = authenticationTokenDto.authentication().getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().map(a -> a.replace("ROLE_", "")).orElse(null);
        return new JwtResponse(authenticationTokenDto.token(), "Bearer", authenticationTokenDto.authentication().getName(), role);
    }
}