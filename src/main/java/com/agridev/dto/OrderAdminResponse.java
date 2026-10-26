package com.agridev.dto;

import lombok.Data;

import java.time.LocalDateTime;

// DTO class to send order summary details for admin dashboard
@Data
public class OrderAdminResponse {

    private Long id;

    private String buyerName;

    private double totalAmount;

    private String status;

    private LocalDateTime createdAt;

}
