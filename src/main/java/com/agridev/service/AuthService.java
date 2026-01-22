package com.agridev.service;

import com.agridev.dto.*;
import com.agridev.exception.UserNotFound;
import com.agridev.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agridev.model.Role;
import com.agridev.model.User;
import com.agridev.utils.JwtUtil;

import lombok.RequiredArgsConstructor;

// Service class to handle authentication business logic
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final ModelMapper mapper;

    // Method to register new user in the system
    public RegisterResponseDTO registerUser(UserRegistetionDTO dto) {

        if (userRepo.findByEmail(dto.getEmail()).isPresent())
            throw new IllegalArgumentException("Email already exists");

        User user = mapper.map(dto, User.class);

        user.setPassword(encoder.encode(dto.getPassword()));
        if (dto.getRole() != null) {
            user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
        }


        if (user.getAddress() != null)
            user.getAddress().setUser(user);

        userRepo.save(user);

        UserDTO userDTO = mapper.map(user, UserDTO.class);
        return new RegisterResponseDTO(
                "User registered successfully",
                user.getRole().toString(),
                userDTO
        );
    }

    // Method to authenticate user and generate login token
    public LoginResponseDTO login(LoginReq loginReq) {

        User user = userRepo.findByEmail(loginReq.getEmail())
                .orElseThrow(() ->
                        new UserNotFound("Invalid email or user not found"));

        if (!encoder.matches(loginReq.getPassword(), user.getPassword()))
            throw new IllegalArgumentException("Invalid password");

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().toString()
        );

        UserDTO userDTO = mapper.map(user, UserDTO.class);

        return new LoginResponseDTO(token, "Login successful", userDTO);
    }

}
