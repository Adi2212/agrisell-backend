package com.agridev.dto;

import lombok.Data;

import java.time.LocalDateTime;

// DTO class to send order status history details for admin view
@Data
public class OrderStatusHistoryResponse {

    private Long id;

    private String status;

    private LocalDateTime changedAt;

}
