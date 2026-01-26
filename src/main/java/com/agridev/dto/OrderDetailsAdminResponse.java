package com.agridev.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// DTO class to send complete order details for admin view
@Data
public class OrderDetailsAdminResponse {

    private Long id;

    private String buyerName;

    private Double totalAmount;

    private String paymentMethod;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private OrderAddressResponse deliveryAddress;

    private List<OrderItemAdminResponse> items;

    private List<OrderStatusHistoryResponse> history;

}
