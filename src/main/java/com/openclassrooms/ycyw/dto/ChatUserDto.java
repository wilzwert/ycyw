package com.openclassrooms.ycyw.dto;


import jakarta.annotation.Nullable;

import java.util.UUID;

/**
 * @author Wilhelm Zwertvaegher
 * Date:11/02/2025
 * Time:21:12
 */

public record ChatUserDto (String username, @Nullable UUID conversationId) {}
