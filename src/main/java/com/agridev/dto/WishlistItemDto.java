package com.agridev.dto;

import lombok.Data;

@Data
public class WishlistItemDto {
    private Long id; // Product ID
    private String name;
    private double price;
    private String imgUrl;
}
