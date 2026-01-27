package com.agridev.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.util.List;

// DTO class to receive payment request details
@Data
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotEmpty(message = "Payment items cannot be empty")
    @Valid
    private List<OrderItemRequest> items;
}
