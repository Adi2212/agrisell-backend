package com.agridev.dto;

import lombok.Data;

// DTO class to receive order item request details
@Data
public class OrderItemRequest {

    private Long productId;

    private int quantity;

}
