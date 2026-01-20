package com.agridev.dto;

import lombok.Getter;
import lombok.Setter;

// DTO for login request data
@Getter
@Setter
public class LoginReq {

    private String email;

    private String password;

}
