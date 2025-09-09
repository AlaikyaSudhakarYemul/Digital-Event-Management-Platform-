package com.wipro.demp.config;

import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.wipro.demp.entity.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
@Component
public class JwtUtil {
 
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private final SecretKey secretKey;
    private final long expirationTime;
    private final JwtParser jwtParser;
 
    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret key is missing or empty.");
        }
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.expirationTime = expiration;
        this.jwtParser = Jwts.parser().setSigningKey(secretKey).build();
        logger.info("JWT Secret Key initialized successfully.");
    }
 
    public String generateToken(String username, Role role) {
        String token = Jwts.builder()
            .setSubject(username)
            .claim("role", "ROLE_" + role.name())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
 
        logger.info(" Generated JWT Token: {}", token);
        return token;
    }
 
    public String extractEmail(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? claims.getSubject() : null;
    }
 
    public String extractRole(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? claims.get("role", String.class) : null;
    }
 
    private Claims extractClaims(String token) {
        token = token.replace("Bearer ", "").trim();
        logger.debug("Extracting claims from token: {}", token);
        // Let exceptions propagate
        return jwtParser.parseClaimsJws(token).getBody();
    }
}