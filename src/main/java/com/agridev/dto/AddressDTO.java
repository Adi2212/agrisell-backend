package com.agridev.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    @NotBlank(message = "Street is required")
    @Size(min = 3, max = 150, message = "Street must be between 3 and 150 characters")
    private String street;

    @NotBlank(message = "City is required")
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters")
    private String state;

    @NotBlank(message = "District is required")
    @Size(min = 2, max = 50, message = "District must be between 2 and 50 characters")
    private String district;

    @NotBlank(message = "Postal code is required")
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "Postal code must be exactly 6 digits"
    )
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 50, message = "Country must be between 2 and 50 characters")
    private String country;
}
