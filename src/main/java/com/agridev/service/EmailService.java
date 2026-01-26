package com.agridev.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetToken(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset OTP");

        message.setText(
                "Your OTP for resetting password is:\n\n"
                        + otp +
                        "\n\nOTP valid for 10 minutes."
        );

        mailSender.send(message);
    }

}
