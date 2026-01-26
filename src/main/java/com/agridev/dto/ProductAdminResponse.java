package com.agridev.dto;

import lombok.Data;

// DTO class to send product details for admin dashboard
@Data
public class ProductAdminResponse {

    private Long id;

    private String name;

    private String farmerName;

    private double price;

    private int stock;

}
