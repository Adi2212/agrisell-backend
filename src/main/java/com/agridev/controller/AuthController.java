package com.agridev.controller;

import com.agridev.dto.*;
import com.agridev.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

// Controller to handle authentication related APIs like register and login
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Register API
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> registerUser(
            @Valid @RequestBody UserRegistetionDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerUser(dto));
    }

    // Login API
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginReq loginReq) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.login(loginReq));
    }

    // Forgot Password API
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO request) {

        return ResponseEntity.ok(
                authService.forgotPassword(request.getEmail())
        );
    }

    // Reset Password API
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO request) {

        return ResponseEntity.ok(
                authService.resetPassword(
                        request.getToken(),
                        request.getNewPassword()
                )
        );
    }
}
