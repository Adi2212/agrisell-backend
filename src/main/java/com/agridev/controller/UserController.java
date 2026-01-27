package com.agridev.controller;

import com.agridev.dto.AddressDTO;
import com.agridev.dto.UserDTO;
import com.agridev.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// Controller to handle user related operations
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    // Get user details by id
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @PathVariable @Min(value = 1, message = "User ID must be positive") Long id) {

        return ResponseEntity.ok(userService.getUserById(id));
    }

    // Update user profile details (PATCH)
    @PatchMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
            @Valid @RequestBody UserDTO userDTO,
            HttpServletRequest request) {

        return ResponseEntity.ok(userService.updateProfile(userDTO, request));
    }

    // Update user address
    @PutMapping("/address")
    public ResponseEntity<UserDTO> setUsersAddress(
            @Valid @RequestBody AddressDTO addressDTO,
            HttpServletRequest request) {

        return ResponseEntity.ok(userService.setUserAddress(addressDTO, request));
    }

    // Update user profile photo
    @PutMapping("/profile-photo")
    public ResponseEntity<UserDTO> saveProfilePhoto(
            @Valid @RequestBody UserDTO userDTO,
            HttpServletRequest request) {

        return ResponseEntity.ok(userService.updateProfilePhoto(userDTO, request));
    }
}
