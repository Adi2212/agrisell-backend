package com.agridev.controller;

import com.agridev.dto.AddressDTO;
import com.agridev.dto.UserDTO;
import com.agridev.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller to handle user related operations
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // API to get user details by id
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // API to update user profile details
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
            @RequestBody UserDTO userDTO,
            HttpServletRequest request) {

        return ResponseEntity.ok(userService.updateProfile(userDTO, request));
    }

    // API to update user address
    @PutMapping("/address")
    public ResponseEntity<UserDTO> setUsersAddress(
            @RequestBody AddressDTO addressDTO,
            HttpServletRequest request) {

        return ResponseEntity.ok(userService.setUserAddress(addressDTO, request));
    }

    // API to update user profile photo
    @PutMapping("/profile-photo")
    public ResponseEntity<UserDTO> saveProfilePhoto(
            @RequestBody UserDTO userDTO,
            HttpServletRequest request) {

        return ResponseEntity.ok(userService.updateProfilePhoto(userDTO, request));
    }

}
