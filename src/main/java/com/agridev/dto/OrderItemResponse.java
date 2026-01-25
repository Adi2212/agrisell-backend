package com.agridev.dto;

import lombok.Data;

// DTO class to send order item details in response
@Data
public class OrderItemResponse {

    private Long productId;

    private String productName;

    private int quantity;

    private double price;

    private double lineTotal;

}
