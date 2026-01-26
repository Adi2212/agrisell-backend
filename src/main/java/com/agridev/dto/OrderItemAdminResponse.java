package com.agridev.dto;

import lombok.Data;

// DTO class to send order item details in admin order view
@Data
public class OrderItemAdminResponse {

    private Long id;

    private String productName;

    private Long productId;

    private int quantity;

    private Double price;

    private OrderAddressResponse pickUpAddress;

}
