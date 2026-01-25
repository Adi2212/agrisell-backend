package com.agridev.dto;

import com.agridev.model.PaymentStatus;
import com.agridev.model.Status;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// DTO class to send order response details
@Data
public class OrderResponse {

    private Long orderId;

    private Double totalAmount;

    private Status orderStatus;

    private PaymentStatus paymentStatus;

    private AddressResponse deliveryAddress;

    private List<OrderItemResponse> items;

    private LocalDateTime createdAt;

}
