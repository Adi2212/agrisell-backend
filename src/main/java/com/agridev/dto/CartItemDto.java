package com.agridev.dto;

import lombok.Data;

@Data
public class CartItemDto {
    private Long id; // Represents Product ID for frontend compatibility
    private String name;
    private double price;
    private String imgUrl;
    private int qty;
}
