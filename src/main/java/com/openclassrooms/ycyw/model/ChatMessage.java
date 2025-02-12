package com.openclassrooms.ycyw.model;


/**
 * @author Wilhelm Zwertvaegher
 * Date:05/02/2025
 * Time:15:59
 */

public record ChatMessage(String sender, String recipient, ChatMessageType type, String content) {}
