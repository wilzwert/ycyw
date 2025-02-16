package com.openclassrooms.ycyw.model;


import jakarta.annotation.Nullable;

import java.util.UUID;

/**
 * @author Wilhelm Zwertvaegher
 * Date:05/02/2025
 * Time:15:59
 */

public record ChatMessage(String sender, String recipient, ChatMessageType type, String content, @Nullable UUID conversationId) {}
