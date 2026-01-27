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
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ModelMapper mapper;

    // REGISTER
    public RegisterResponseDTO registerUser(UserRegistetionDTO dto) {

        log.info("Register request received for email: {}", dto.getEmail());

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            log.warn("Registration failed: Email already exists -> {}", dto.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        User user = mapper.map(dto, User.class);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getRole() != null) {
            user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
            log.info("Assigned role: {}", user.getRole());
        }

        if (user.getAddress() != null) {
            user.getAddress().setUser(user);
            log.debug("Address mapped successfully for user: {}", dto.getEmail());
        }

        userRepository.save(user);

        log.info("User registered successfully with ID: {}", user.getId());

        return new RegisterResponseDTO(
                "User registered successfully",
                user.getRole().name(),
                mapper.map(user, UserDTO.class)
        );
    }

    // LOGIN
    public LoginResponseDTO login(LoginReq loginReq) {

        log.info("Login attempt for email: {}", loginReq.getEmail());

        User user = userRepository.findByEmail(loginReq.getEmail())
                .orElseThrow(() -> {
                    log.error("Login failed: User not found -> {}", loginReq.getEmail());
                    return new UserNotFound("Invalid email or user not found");
                });

        if (!passwordEncoder.matches(loginReq.getPassword(), user.getPassword())) {
            log.warn("Invalid password attempt for email: {}", loginReq.getEmail());
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        log.info("Login successful for userId: {}", user.getId());

        return new LoginResponseDTO(
                token,
                "Login successful",
                mapper.map(user, UserDTO.class)
        );
    }

    // FORGOT PASSWORD
    public String forgotPassword(String email) {

        log.info("Forgot password request received for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Forgot password failed: Email not registered -> {}", email);
                    return new UserNotFound("Email not registered");
                });

        String otp = generateOtp();

        PasswordResetToken resetToken =
                tokenRepo.findByUser(user).orElse(
                        PasswordResetToken.builder()
                                .user(user)
                                .build()
                );

        resetToken.setToken(otp);
        resetToken.setExpiryTime(LocalDateTime.now().plusMinutes(10));

        tokenRepo.save(resetToken);

        log.info("OTP generated and stored for userId: {}", user.getId());

        emailService.sendResetToken(email, otp);

        log.info("OTP sent successfully to email: {}", email);

        return "OTP sent successfully";
    }

    // RESET PASSWORD
    public String resetPassword(String token, String newPassword) {

        log.info("Password reset attempt with token");

        PasswordResetToken resetToken = tokenRepo.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Invalid reset token attempt");
                    return new IllegalArgumentException("Invalid token");
                });

        if (resetToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.warn("Reset token expired for userId: {}", resetToken.getUser().getId());
            throw new IllegalArgumentException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        log.info("Password updated successfully for userId: {}", user.getId());

        tokenRepo.delete(resetToken);

        log.debug("Reset token deleted after successful password reset");

        return "Password reset successful";
    }

    // OTP Generator
    private String generateOtp() {
        log.debug("Generating OTP...");
        return String.format("%06d", new Random().nextInt(999999));
    }

}
