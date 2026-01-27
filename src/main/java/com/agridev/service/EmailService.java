package com.agridev.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetToken(String email, String otp) {

        log.info("Sending password reset OTP email to: {}", email);

        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset OTP");

            message.setText(
                    "Your OTP for resetting password is:\n\n"
                            + otp +
                            "\n\nOTP valid for 10 minutes."
            );

            mailSender.send(message);

            log.info("Password reset OTP email sent successfully to: {}", email);

        } catch (Exception e) {

            log.error("Failed to send OTP email to: {}. Error: {}", email, e.getMessage());
            throw new RuntimeException("Email sending failed. Please try again later.");
        }
    }
}
