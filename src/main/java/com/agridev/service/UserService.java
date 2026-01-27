package com.agridev.service;

import com.agridev.dto.AddressDTO;
import com.agridev.dto.UserDTO;
import com.agridev.exception.UserNotFound;
import com.agridev.model.Address;
import com.agridev.model.User;
import com.agridev.repository.UserRepository;
import com.agridev.utils.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Service class to handle user related business logic
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepo;
    private final ModelMapper mapper;
    private final JwtUtil jwtUtil;

    // Get user details by user id
    public UserDTO getUserById(Long id) {

        log.info("Fetching user details for userId: {}", id);

        User user = userRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new UserNotFound("User not found");
                });

        log.info("User details fetched successfully for userId: {}", id);

        return mapper.map(user, UserDTO.class);
    }

    // Update logged-in user's profile details
    public UserDTO updateProfile(UserDTO userDTO, HttpServletRequest request) {

        Long userId = jwtUtil.extractUserId(jwtUtil.extractToken(request));

        log.info("Update profile request received for userId: {}", userId);

        User user = userRepo.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found for profile update. userId: {}", userId);
                    return new UserNotFound("User not found");
                });

        // Update only provided fields
        if (userDTO.getName() != null) {
            log.debug("Updating name for userId: {}", userId);
            user.setName(userDTO.getName());
        }

        if (userDTO.getPhone() != null) {
            log.debug("Updating phone for userId: {}", userId);
            user.setPhone(userDTO.getPhone());
        }

        if (userDTO.getProfileUrl() != null) {
            log.debug("Updating profile URL for userId: {}", userId);
            user.setProfileUrl(userDTO.getProfileUrl());
        }

        User savedUser = userRepo.save(user);

        log.info("Profile updated successfully for userId: {}", userId);

        return mapper.map(savedUser, UserDTO.class);
    }

    // Update or set logged-in user's address
    public UserDTO setUserAddress(AddressDTO addressDTO, HttpServletRequest request) {

        Long userId = jwtUtil.extractUserId(jwtUtil.extractToken(request));

        log.info("Set address request received for userId: {}", userId);

        User user = userRepo.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found for address update. userId: {}", userId);
                    return new UserNotFound("User not found");
                });

        Address address = user.getAddress();

        if (address == null) {
            log.info("No existing address found. Creating new address for userId: {}", userId);
            address = new Address();
            address.setUser(user);
        }

        mapper.map(addressDTO, address);
        user.setAddress(address);

        User savedUser = userRepo.save(user);

        log.info("Address updated successfully for userId: {}", userId);

        return mapper.map(savedUser, UserDTO.class);
    }

    // Update logged-in user's profile photo
    public UserDTO updateProfilePhoto(UserDTO userDTO, HttpServletRequest request) {

        Long userId = jwtUtil.extractUserId(jwtUtil.extractToken(request));

        log.info("Update profile photo request received for userId: {}", userId);

        User user = userRepo.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found for profile photo update. userId: {}", userId);
                    return new UserNotFound("User not found");
                });

        user.setProfileUrl(userDTO.getProfileUrl());

        User savedUser = userRepo.save(user);

        log.info("Profile photo updated successfully for userId: {}", userId);

        return mapper.map(savedUser, UserDTO.class);
    }
}
