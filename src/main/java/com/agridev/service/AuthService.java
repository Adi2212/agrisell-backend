package com.agridev.service;

import com.agridev.exception.UserNotFound;
import com.agridev.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agridev.dto.LoginReq;
import com.agridev.dto.LoginResponseDTO;
import com.agridev.dto.UserDTO;
import com.agridev.dto.UserRegistetionDTO;
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

    // Method to register new farmer user in the system
    public LoginResponseDTO registerFarmer(UserRegistetionDTO dto) {

        if (userRepo.findByEmail(dto.getEmail()).isPresent())
            throw new IllegalArgumentException("Email already exists");

        User user = mapper.map(dto, User.class);

        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRole(Role.FARMER);

        if (user.getAddress() != null)
            user.getAddress().setUser(user);

        userRepo.save(user);

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().toString()
        );

        UserDTO userDTO = mapper.map(user, UserDTO.class);

        return new LoginResponseDTO(token, "Farmer registered successfully", userDTO);
    }

    // Method to register new buyer user in the system
    public LoginResponseDTO registerBuyer(UserRegistetionDTO dto) {

        if (userRepo.findByEmail(dto.getEmail()).isPresent())
            throw new IllegalArgumentException("Email already exists");

        User user = mapper.map(dto, User.class);

        user.setPassword(encoder.encode(dto.getPassword()));
        user.setRole(Role.BUYER);

        if (user.getAddress() != null)
            user.getAddress().setUser(user);

        userRepo.save(user);

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().toString()
        );

        UserDTO userDTO = mapper.map(user, UserDTO.class);

        return new LoginResponseDTO(token, "Buyer registered successfully", userDTO);
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
