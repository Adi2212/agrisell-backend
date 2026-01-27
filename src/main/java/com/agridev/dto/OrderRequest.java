package com.agridev.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import lombok.Data;

import java.util.List;

// DTO class to receive order request data from client
@Data
public class OrderRequest {

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemRequest> items;
}
