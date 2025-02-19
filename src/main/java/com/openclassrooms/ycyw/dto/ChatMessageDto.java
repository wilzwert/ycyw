package com.openclassrooms.ycyw.dto;


import com.openclassrooms.ycyw.model.ChatMessageType;
import jakarta.annotation.Nullable;

import java.util.UUID;

/**
 * @author Wilhelm Zwertvaegher
 * Date:05/02/2025
 * Time:15:59
 */

public record ChatMessageDto(String sender, String recipient, ChatMessageType type, String content, @Nullable UUID conversationId) {}