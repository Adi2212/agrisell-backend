package com.agridev.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    // Main Security Filter Chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                // Disable CSRF (JWT is Stateless)
                .csrf(csrf -> csrf.disable())

                // Unauthorized Handler
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(jwtAuthEntryPoint)
                )

                // Session should be Stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authorization Rules
                .authorizeHttpRequests(auth -> auth

                        // Allow Preflight Requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public APIs (No Token Required)
                        .requestMatchers(
                                "/auth/**",
                                "/auth/forgot-password",
                                "/auth/reset-password",
                                "/actuator/**",
                                "/imagekit/auth",
                                "/reviews/**"
                        ).permitAll()

                        //Swagger Api
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Public Product Browsing
                        .requestMatchers(
                                "/products/get/**",
                                "/categories/**"
                        ).permitAll()

                        // Orders and Payment Public Access (if needed)
                        .requestMatchers(
                                "/orders/create",
                                "/payments/checkout"
                        ).permitAll()

                        // FARMER Only
                        .requestMatchers("/products/stats")
                        .hasRole("FARMER")

                        // FARMER + ADMIN Access
                        .requestMatchers(
                                "/products/add",
                                "/products/farmer",
                                "/products/update/**",
                                "/products/delete/**"
                        ).hasAnyRole("FARMER", "ADMIN")

                        // ADMIN Only Access
                        .requestMatchers(
                                "/categories/add",
                                "/categories/status",
                                "/admin/**"
                        ).hasRole("ADMIN")

                        // FARMER + BUYER Access
                        .requestMatchers("/user/address")
                        .hasAnyRole("FARMER", "BUYER")

                        // All Other Requests Require JWT
                        .anyRequest().authenticated()
                )

                // Add JWT Filter Before UsernamePassword Filter
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
