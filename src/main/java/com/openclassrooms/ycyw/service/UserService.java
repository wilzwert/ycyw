package com.openclassrooms.ycyw.service;


import com.openclassrooms.ycyw.dto.controller.AuthenticationTokenDto;
import com.openclassrooms.ycyw.dto.request.LoginRequestDto;
import com.openclassrooms.ycyw.model.User;
import org.springframework.security.core.Authentication;

import java.util.Optional;

/**
 * @author Wilhelm Zwertvaegher
 * Date:17/02/2025
 * Time:21:13
 */

public interface UserService {

    Optional<User> findUserByUsername(final String username);

    AuthenticationTokenDto login(Authentication authentication, final LoginRequestDto loginRequestDto);

    AuthenticationTokenDto upgradeAuthentication(Authentication authentication, final LoginRequestDto loginRequestDto);

    Authentication authenticateUser(String username, String password) ;

    Authentication authenticateAnonymously();


    /**
     *
     * @return a generated username
     */
    String generateUsername();
}