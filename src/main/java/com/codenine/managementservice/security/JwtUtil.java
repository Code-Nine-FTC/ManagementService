package com.codenine.managementservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.codenine.managementservice.dto.user.Role;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final Key key;
    private final long expiration = 1000 * 60 * 60 * 10; // 10 horas

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, Role role, List<Long> sectionIds) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.toString())
                .claim("sectionsIds", sectionIds)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public Role extractUserRole(String token) {
        return extractClaims(token).get("role", Role.class);
    }

    public List<Long> extractUserSectionIds(String token) {
        return extractClaims(token).get("sectionsIds", List.class);
    }

    public String extractUserEmail(String token) {
        return extractClaims(token).getSubject();
    }
}