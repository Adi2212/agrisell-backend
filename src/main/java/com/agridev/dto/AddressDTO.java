package com.agridev.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private String street;

    private String city;

    private String state;

    private String district;

    private String postalCode;

    private String country;

}