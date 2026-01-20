package com.agridev.dto;

import com.agridev.model.Address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO for user registration request
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistetionDTO {

    private Long id;

    private String name;

    private String email;

    private String password;

    private String role;

    private String phone;

    private Address address;

}
