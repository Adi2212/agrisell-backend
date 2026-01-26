package com.agridev.service;

import com.agridev.dto.*;
import com.agridev.exception.UserNotFound;
import com.agridev.model.PasswordResetToken;
import com.agridev.model.Role;
import com.agridev.model.User;
import com.agridev.repository.PasswordResetTokenRepository;
import com.agridev.repository.UserRepository;
import com.agridev.utils.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ModelMapper mapper;

    // REGISTER
    public RegisterResponseDTO registerUser(UserRegistetionDTO dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent())
            throw new IllegalArgumentException("Email already exists");

        User user = mapper.map(dto, User.class);

        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getRole() != null)
            user.setRole(Role.valueOf(dto.getRole().toUpperCase()));

        if (user.getAddress() != null)
            user.getAddress().setUser(user);

        userRepository.save(user);

        return new RegisterResponseDTO(
                "User registered successfully",
                user.getRole().name(),
                mapper.map(user, UserDTO.class)
        );
    }

    // LOGIN
    public LoginResponseDTO login(LoginReq loginReq) {

        User user = userRepository.findByEmail(loginReq.getEmail())
                .orElseThrow(() ->
                        new UserNotFound("Invalid email or user not found"));

        if (!passwordEncoder.matches(loginReq.getPassword(), user.getPassword()))
            throw new IllegalArgumentException("Invalid password");

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        return new LoginResponseDTO(
                token,
                "Login successful",
                mapper.map(user, UserDTO.class)
        );
    }

    public String forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("Email not registered"));


        String otp = String.format("%06d", new Random().nextInt(999999));


        PasswordResetToken resetToken =
                tokenRepo.findByUser(user).orElse(
                        PasswordResetToken.builder()
                                .user(user)
                                .build()
                );

        resetToken.setToken(otp);
        resetToken.setExpiryTime(LocalDateTime.now().plusMinutes(10));

        tokenRepo.save(resetToken);

        emailService.sendResetToken(email, otp);

        return "OTP sent successfully";
    }



    public String resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = tokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (resetToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        User user = resetToken.getUser();

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete token after use
        tokenRepo.delete(resetToken);

        return "Password reset successful";
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }


}
