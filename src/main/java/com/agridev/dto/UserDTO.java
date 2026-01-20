package com.agridev.dto;

import com.agridev.model.AccStatus;
import com.agridev.model.Address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO class to transfer user data between layers
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private String role;

    private String profileUrl;

    private AddressDTO address;

    private AccStatus accStatus;

}
