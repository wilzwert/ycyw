package com.openclassrooms.ycyw.model;


import io.jsonwebtoken.Claims;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a JWT token
 * @author Wilhelm Zwertvaegher
 * Date:02/16/2025
 * Time:16:20
 */
@Getter
@Setter
@AllArgsConstructor
public class JwtToken {
    @NotNull
    private String subject;
    private Claims claims;
}