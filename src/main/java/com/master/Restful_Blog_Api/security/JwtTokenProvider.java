package com.master.Restful_Blog_Api.security;

import com.master.Restful_Blog_Api.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private SecretKey signingKey;

    // Signing

    // Generate secret key from configuration
    // Initialize and caches signing key at startup
    @PostConstruct
    void init() {
        byte[] keyBytes = jwtConfig.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        if(keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes for HS256, got " + keyBytes.length);
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT signing key initialized successfully");
    }

    public String generateToken(String email, Long userId, String role) {
        log.debug("Generating JWT: email={}, userId={}, role={}", email, userId, role);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        // create token
        Date now = new Date();
        Date expirydDate = new Date(now.getTime() + jwtConfig.getJwtExpiration());

        String token = Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(now)
                .expiration(expirydDate)
                .signWith(signingKey)
                .compact();

        log.debug("JWT generated for email={} | expires at {}", email, expirydDate);
        return token;
    }

    // ==================================================================

    public String getEmailFromToken(String token) throws JwtException {
        return getAllClaimsFromToken(token).getSubject();
    }

    public Long getUserIdFromToken(String token) throws JwtException {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    public String getRoleFromToken(String token) throws JwtException {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    public Date getExpirationDateFromToken(String token) throws JwtException {
        return getAllClaimsFromToken(token).getExpiration();
    }


    public TokenData parseToken(String token) throws JwtException {
        Claims claims = getAllClaimsFromToken(token);
        return new TokenData(
                claims.getSubject(),
                claims.get("userId", Long.class),
                claims.get("role", String.class)
        );
    }


    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                /// Calculates & Compares Signature
                /// If valid getPayload
                .parseSignedClaims(token)
                .getPayload();
    }

    // Verifying

    public Boolean validateToken(String token, String email) {
        /// If signature is invalid throws Exception immediately
        try {
            final String tokenEmail = getEmailFromToken(token);

            if(!tokenEmail.equals(email))  {
                log.warn("JWT email mismatch: expected={}, inToken={}", email, tokenEmail);
                return false;
            }

            log.debug("JWT valid for email={}", email);
            return true;
        }
        catch(ExpiredJwtException ex) {
            log.warn("JWT expired: email={}", ex.getClaims().getSubject());
        }
        catch(SignatureException ex) {
            // with different secret
            log.warn("JWT signature invalid - possible token tampering. email={}", email);
        }
        catch(MalformedJwtException ex) {
            // token is not even a valid JWT format
            log.warn("JWT malformed - bad token format received. email={}", email);
        }
        catch(JwtException ex) {
            // any other JWT-related exception
            log.warn("JWT validation failed: {} | email={}", ex.getMessage(), email);
        }
        return false;
    }

    // DTO hold token data
    public record TokenData(String email, Long userId, String role) {}

}
