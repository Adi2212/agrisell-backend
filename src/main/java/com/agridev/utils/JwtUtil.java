package com.agridev.utils;

import java.security.Key;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

// Utility class for generating and validating JWT tokens
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;

    // Method to generate signing key using secret key
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Method to generate JWT token with user details as claims
    public String generateToken(Long userId, String email, String role) {

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Method to extract JWT token from Authorization header
    public String extractToken(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        throw new RuntimeException("Missing or invalid Authorization header");
    }

    // Method to extract username from JWT token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Method to extract user id from JWT token
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    // Method to extract role from JWT token
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // Method to validate whether token is expired or not
    public boolean isTokenValid(String token) {

        try {
            return !extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // Method to extract all claims from JWT token
    public Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
