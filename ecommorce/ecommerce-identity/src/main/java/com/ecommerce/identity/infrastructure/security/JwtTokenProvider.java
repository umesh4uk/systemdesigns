package com.ecommerce.identity.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Stateless JWT utility. Produces and validates access/refresh tokens.
 * Secrets are loaded from config — never hard-coded.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(UUID customerId, String email, List<String> roles) {
        return buildToken(customerId, email, roles, jwtProperties.getAccessTokenExpirySeconds(), "access");
    }

    public String generateRefreshToken(UUID customerId) {
        return buildToken(customerId, null, List.of(), jwtProperties.getRefreshTokenExpirySeconds(), "refresh");
    }

    private String buildToken(UUID customerId, String email, List<String> roles,
                               long expirySeconds, String tokenType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirySeconds * 1000);

        JwtBuilder builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(customerId.toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .claim("type", tokenType)
                .signWith(getSigningKey());

        if (email != null) builder.claim("email", email);
        if (!roles.isEmpty()) builder.claim("roles", roles);

        return builder.compact();
    }

    public Claims validateAndParseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractCustomerId(String token) {
        return UUID.fromString(validateAndParseClaims(token).getSubject());
    }

    public boolean isValid(String token) {
        try {
            validateAndParseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = validateAndParseClaims(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
