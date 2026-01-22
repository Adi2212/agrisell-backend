package com.agridev.controller;

import com.agridev.dto.LoginReq;
import com.agridev.dto.LoginResponseDTO;
import com.agridev.dto.RegisterResponseDTO;
import com.agridev.dto.UserRegistetionDTO;
import com.agridev.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agridev.service.AuthService;

import lombok.RequiredArgsConstructor;

// Controller to handle authentication related APIs like register and login
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    // Method to register a new farmer user in the system
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> registerUser(
            @RequestBody UserRegistetionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(dto));
    }

    // Method to authenticate user and return login response with JWT token
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginReq loginReq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.login(loginReq));
    }

}
