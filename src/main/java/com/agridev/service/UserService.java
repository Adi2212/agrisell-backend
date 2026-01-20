package com.agridev.service;

import com.agridev.dto.UserDTO;
import com.agridev.model.User;
import com.agridev.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper ;

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();

        List<UserDTO> userDTOs = users.stream()
                .map(u -> modelMapper.map(u, UserDTO.class))
                .collect(Collectors.toList());

        return userDTOs;
    }

}
