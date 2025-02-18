package com.openclassrooms.ycyw.dto.controller;


import org.springframework.security.core.Authentication;

/**
 * @author Wilhelm Zwertvaegher
 * Date:18/02/2025
 * Time:08:47
 */

public record AuthenticationTokenDto(Authentication authentication, String token) {}
