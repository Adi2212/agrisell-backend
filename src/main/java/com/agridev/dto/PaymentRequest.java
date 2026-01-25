package com.agridev.dto;

import lombok.Data;

import java.util.List;

// DTO class to receive payment request details
@Data
public class PaymentRequest {

    private Long orderId;

    private List<OrderItemRequest> items;

}
