package com.agridev.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTO class to send order status history details for admin and buyer view
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistoryResponse {

    private Long id;

    private String status;

    private LocalDateTime changedAt;

    private LocalDateTime createdAt;

}
