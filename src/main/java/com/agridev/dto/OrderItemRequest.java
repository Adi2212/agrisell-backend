package com.agridev.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

// DTO class to receive order item request details
@Data
public class OrderItemRequest {

    @NotNull(message = "Product details are required")
    @Valid
    private ProductItem product;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
