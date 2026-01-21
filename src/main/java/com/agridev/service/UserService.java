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

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Service class to handle user related business logic
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final ModelMapper mapper;
    private final JwtUtil jwtUtil;

    // Get user details by user id
    public UserDTO getUserById(Long id) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFound("User not found"));

        return mapper.map(user, UserDTO.class);
    }

    // Update logged-in user's profile details
    public UserDTO updateProfile(UserDTO userDTO, HttpServletRequest request) {

        Long userId = jwtUtil.extractUserId(jwtUtil.extractToken(request));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFound("User not found"));

        user.setName(userDTO.getName());
        user.setPhone(userDTO.getPhone());

        User savedUser = userRepo.save(user);
        return mapper.map(savedUser, UserDTO.class);
    }

    // Update or set logged-in user's address
    public UserDTO setUserAddress(AddressDTO addressDTO, HttpServletRequest request) {

        Long userId = jwtUtil.extractUserId(jwtUtil.extractToken(request));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFound("User not found"));

        Address address = user.getAddress();

        if (address == null) {
            address = new Address();
            address.setUser(user);
        }

        mapper.map(addressDTO, address);
        user.setAddress(address);

        User savedUser = userRepo.save(user);
        return mapper.map(savedUser, UserDTO.class);
    }

    // Update logged-in user's profile photo
    public UserDTO updateProfilePhoto(UserDTO userDTO, HttpServletRequest request) {

        Long userId = jwtUtil.extractUserId(jwtUtil.extractToken(request));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFound("User not found"));

        user.setProfileUrl(userDTO.getProfileUrl());

        User savedUser = userRepo.save(user);
        return mapper.map(savedUser, UserDTO.class);
    }

}
