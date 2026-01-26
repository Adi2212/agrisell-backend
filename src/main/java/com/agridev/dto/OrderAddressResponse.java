package com.agridev.dto;

import lombok.Data;

// DTO class to send order address details in admin responses
@Data
public class OrderAddressResponse {

    private String street;

    private String city;

    private String district;

    private String state;

    private String postalCode;

}
