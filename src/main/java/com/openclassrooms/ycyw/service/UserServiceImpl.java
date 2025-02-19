package com.openclassrooms.ycyw.service;

import com.openclassrooms.ycyw.dto.controller.AuthenticationTokenDto;
import com.openclassrooms.ycyw.dto.request.LoginRequestDto;
import com.openclassrooms.ycyw.model.User;
import com.openclassrooms.ycyw.repository.UserRepository;
import com.openclassrooms.ycyw.security.AuthenticationType;
import com.openclassrooms.ycyw.security.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserServiceImpl(UserRepository userRepository, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public Optional<User> findUserByUsername(final String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public AuthenticationTokenDto login(Authentication authentication, LoginRequestDto loginRequestDto) {
        if(authentication != null) {
            return this.upgradeAuthentication(authentication, loginRequestDto);
        }

        AuthenticationType authType = AuthenticationType.ANONYMOUS;

        // no pre-existing authentication, login request sent
        if(loginRequestDto != null) {
            authentication = this.authenticateUser(loginRequestDto.getUsername(), loginRequestDto.getPassword());
            authType = AuthenticationType.USER;
        }
        else {
            authentication = this.authenticateAnonymously();
        }

        return new AuthenticationTokenDto(authentication, jwtService.generateToken(authentication.getName(), authType));
    }

    @Override
    public AuthenticationTokenDto upgradeAuthentication(Authentication authentication, LoginRequestDto loginRequestDto) {
        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toCollection(ArrayList::new));

        // no login upgrade if current authentication is not anonymous
        if(null != loginRequestDto && !authorities.contains("ROLE_ANONYMOUS")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        if(null != loginRequestDto && authorities.contains("ROLE_ANONYMOUS")) {
            authentication = this.authenticateUser(loginRequestDto.getUsername(), loginRequestDto.getPassword());
        }

        // generated new token and return the data
        return new AuthenticationTokenDto(authentication, jwtService.generateToken(authentication.getName(), authorities.contains("ANONYMOUS") ? AuthenticationType.ANONYMOUS : AuthenticationType.USER));
    }


    public Authentication authenticateUser(String username, String password) throws AuthenticationException {
        Optional<User> user = findUserByUsername(username);
        if(user.isEmpty()) {
            throw new AuthenticationException("User not found") {
                @Override
                public String getMessage() {
                    return super.getMessage();
                }
            };
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.get().getUsername(), password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    public Authentication authenticateAnonymously() throws AuthenticationException {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder().roles("ANONYMOUS").username(this.generateUsername()).password("").build();
        Authentication authentication = new AnonymousAuthenticationToken(userDetails.getUsername(), userDetails, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }


    /**
     *
     * @return a generated username
     */
    public String generateUsername() {
        return UUID.randomUUID().toString();
    }
}
