package com.agridev.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO for user registration request
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistetionDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 80, message = "Name must be between 2 and 80 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(
            regexp = "FARMER|BUYER|ADMIN",
            message = "Role must be FARMER, BUYER, or ADMIN"
    )
    private String role;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone number must be exactly 10 digits"
    )
    private String phone;


    private AddressDTO address;
}
