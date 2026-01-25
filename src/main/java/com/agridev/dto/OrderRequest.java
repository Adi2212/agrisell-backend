package com.agridev.dto;

import lombok.Data;

import java.util.List;

// DTO class to receive order request data from client
@Data
public class OrderRequest {

    private String paymentMethod;

    private List<OrderItemRequest> items;

}
