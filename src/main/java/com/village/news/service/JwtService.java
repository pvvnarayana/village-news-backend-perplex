package com.village.news.service;

import com.village.news.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole())
                .claim("name", user.getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }
    
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.get("userId").toString());
    }
    
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role").toString();
    }
    
    public boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }

            // Remove Bearer prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            System.err.println("Malformed JWT token: " + e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT validation error: " + e.getMessage());
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            // Remove Bearer prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            else if(token.startsWith("jwt-")){
                String[] parts = token.split("-");
                if (parts.length >= 2) {
                    return parts[1]; // Return the email part
                } else {
                    throw new RuntimeException("Invalid JWT format");
                }
            }

            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Invalid or malformed JWT token: " + e.getMessage());
        }
    }

}
