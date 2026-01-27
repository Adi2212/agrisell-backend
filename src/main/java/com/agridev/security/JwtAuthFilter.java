package com.agridev.security;

import com.agridev.utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        log.debug("JWT Filter triggered for request: {}", requestUri);

        // Extract Authorization header
        String authHeader = request.getHeader("Authorization");

        // If no token present, continue request
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            log.debug("No Authorization header found for request: {}", requestUri);

            filterChain.doFilter(request, response);
            return;
        }

        // Extract token
        String token = authHeader.substring(7);

        // Validate token
        if (!jwtUtil.isTokenValid(token)) {

            log.warn("Invalid JWT token received for request: {}", requestUri);

            filterChain.doFilter(request, response);
            return;
        }

        // Extract email from token
        String email = jwtUtil.extractUsername(token);

        // Authenticate only if not already authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            log.info("Authenticating user with email: {}", email);

            // Load user details from DB
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(email);

            // Create Authentication object
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            // Attach request details
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // Set Authentication in Security Context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("User authenticated successfully: {}", email);

        } else {

            log.debug("Authentication already exists or email is null for request: {}", requestUri);
        }

        // Continue request
        filterChain.doFilter(request, response);
    }
}
