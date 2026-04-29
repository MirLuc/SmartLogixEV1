package com.smartlogix.user.service;

import com.smartlogix.user.config.JwtProperties;
import com.smartlogix.user.domain.Role;
import com.smartlogix.user.domain.UserAccount;
import com.smartlogix.user.dto.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final Key signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenResponse generateToken(UserAccount user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(properties.expirationMinutes()));
        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuer(properties.issuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .claim("roles", user.getRoles().stream().map(Role::name).toList())
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
        return new TokenResponse(token, "Bearer", OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC));
    }

    public Optional<TokenDetails> parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .requireIssuer(properties.issuer())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Optional.of(new TokenDetails(
                    claims.getSubject(),
                    parseRoles(claims),
                    OffsetDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneOffset.UTC)
            ));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Role> parseRoles(Claims claims) {
        Object rawRoles = claims.get("roles");
        if (rawRoles instanceof List<?> roles) {
            return roles.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(Role::valueOf)
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }
}
