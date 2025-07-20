package com.village.news.security;

import com.village.news.entity.User;
import com.village.news.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public JwtAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                String email = extractEmailFromToken(token);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Optional<User> userOptional = userRepository.findByEmail(email);

                    if (userOptional.isPresent()) {
                        User user = userOptional.get();

                        System.out.println("Authenticating user: " + user.getEmail() + " (ID: " + user.getId() + ", Role: " + user.getRole() + ")");

                        List<SimpleGrantedAuthority> authorities = Arrays.asList(
                                new SimpleGrantedAuthority("ROLE_" + user.getRole())
                        );

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(email, null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        System.err.println("User not found in database: " + email);
                    }
                }
            } catch (Exception e) {
                logger.error("JWT Authentication failed: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractEmailFromToken(String token) {
        if (token.startsWith("jwt-")) {
            String[] parts = token.split("-");
            if (parts.length >= 2) {
                return parts[1];
            }
        }
        return null;
    }
}

