package com.openclassrooms.ycyw.security.service;

import com.openclassrooms.ycyw.model.JwtToken;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;


/**
 * Provides JWT token generation and validation
 * @author Wilhelm Zwertvaegher
 * Date:07/11/2024
 * Time:16:06
 */

@Service
@Slf4j
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    /**
     * Extracts a JWT token from the current Request
     * @param request the current request
     * @return the extracted jwt token
     * @throws ExpiredJwtException when JWT token is expired
     * @throws MalformedJwtException when JWT token is malformed
     * @throws IllegalArgumentException when JWT token is illegal
     * @throws UnsupportedJwtException when JWT token is unsupported
     * @throws SignatureException when JWT token's signature is invalid
     */
    public Optional<JwtToken> extractTokenFromRequest(HttpServletRequest request) throws ExpiredJwtException, MalformedJwtException, IllegalArgumentException, UnsupportedJwtException, SignatureException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("Authorization header not found or not compatible with Bearer token");
            return Optional.empty();
        }
        final String token = authHeader.substring(7);

        try {
            Jwt<?, ?> parsedToken = Jwts
                    .parser().verifyWith(getSignInKey()).build().parse(token);
            Claims claims = (Claims) parsedToken.getPayload();

            JwtToken jwtToken = new JwtToken(claims.getSubject(), claims);
            return Optional.of(jwtToken);
        }
        // we only catch different JwtException types to log warning messages
        // the exceptions are then thrown again to be handled by the authentication filter
        catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            throw e;
        }
        catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            throw e;
        }
        catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            throw e;
        }
        catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            throw e;
        }
        catch (IllegalArgumentException e) {
            log.warn("Empty JWT claims: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Generates a token for a user
     * @param username the username we want to generate the token for
     * @return the JWT Token
     */
    public String generateToken(String username) {
        log.info("Generating JWT token for user {}", username);
        return Jwts
                .builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     *
     * @return the signin key
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

