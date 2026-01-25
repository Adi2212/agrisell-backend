package com.agridev.dto;

import lombok.Data;

// DTO class to send address details in response
@Data
public class AddressResponse {

    private String street;

    private String city;

    private String state;

    private String postalCode;

}
