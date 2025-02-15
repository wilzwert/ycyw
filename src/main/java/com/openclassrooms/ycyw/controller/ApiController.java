package com.openclassrooms.ycyw.controller;


import com.openclassrooms.ycyw.dto.ChatUserDto;
import com.openclassrooms.ycyw.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;

/**
 * @author Wilhelm Zwertvaegher
 * Date:05/02/2025
 * Time:15:52
 */

@RestController
public class ApiController {

    private final ChatService chatService;

    private final SimpUserRegistry userRegistry;


    public ApiController(ChatService chatService, SimpUserRegistry simpUserRegistry) {
        this.chatService = chatService;
        this.userRegistry = simpUserRegistry;
    }

    /**
     * Get a list of users
     * @param filter a string defining a filter on the users list
     * @return a list of users using the chat service
     */
    @GetMapping(value= "/api/chat/users", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SUPPORT')")
    public List<String> getUsers(@RequestParam(required = false, defaultValue = "", value="filter") String filter) {
        if(filter.equals("waiting")) {
            return this.chatService.getWaitingUsers().stream().toList();
        }

        return this.userRegistry.getUsers().stream().map(SimpUser::getName).toList();
    }


    @GetMapping(value="/api/chat/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatUserDto retrieveUsername(HttpSession session) {
        Optional<String> sessionUsername = this.chatService.getSessionUsername(session.getId());
        return sessionUsername.map(ChatUserDto::new).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No user"));
    }
}
