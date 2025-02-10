package com.openclassrooms.ycyw.configuration;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Wilhelm Zwertvaegher
 * Date:06/02/2025
 * Time:23:07
 */
public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    private final HashSet<String> usernames = new HashSet<>();


    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails) {
            System.out.println("CustomHandshakeHandler determineUser devrait etre auth ["+auth.getPrincipal().getClass().getName()+"]");
            this.usernames.add(((UserDetails) auth.getPrincipal()).getUsername());
            return auth;
        }

        // Generate principal with UUID as name
        /*
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            String username = httpServletRequest.getParameter("username");
            if (username != null && !username.isEmpty()) {
                System.out.println("CustomHandshakeHandler determineUser "+username);
                // Créer un UserDetails anonyme pour l'utilisateur avec le username
                UserDetails anonymousUser = User.withUsername(username)
                        .password("") // Pas de mot de passe nécessaire pour un anonyme
                        .roles("ANONYMOUS") // Role anonyme
                        .build();

                // Créer un Authentication basé sur cet utilisateur
                Authentication anonymousAuth = new UsernamePasswordAuthenticationToken(anonymousUser, null, anonymousUser.getAuthorities());

                // Retourner cet Authentication, qui sera associé à la session WebSocket
                return anonymousAuth;
            }
        }*/

        // generate anonymous user and prevent collision with existing users
        int c = this.usernames.stream()
                .map(n -> n.replaceAll("^user([0-9]+)", "\1"))
                .map(v -> {try {return Integer.parseInt(v);}catch(NumberFormatException e) {return 0;}})
                .mapToInt(v -> v)
                .max()
                .orElse(0);
        String principalName = "";
        do {
            c++;
            principalName = "user"+c;

        }
        while (this.usernames.contains(principalName));

        String finalPrincipalName = principalName;
        this.usernames.add(finalPrincipalName);
        return new Principal() {
            public String getName() {
                System.out.println("CustomHandshakeHandler determineUser principal "+ finalPrincipalName);
                return finalPrincipalName;
            }
        };
    }
}
