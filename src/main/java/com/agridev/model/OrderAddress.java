package com.agridev.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

// Embedded class to store address details inside order
@Embeddable
@Data
public class OrderAddress {

    private String street;

    private String city;

    private String state;

    private String district;

    private String postalCode;

    private String country;

}
