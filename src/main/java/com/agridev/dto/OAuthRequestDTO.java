package com.agridev.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OAuthRequestDTO {
    @NotBlank(message = "Token is required")
    private String token;
}
