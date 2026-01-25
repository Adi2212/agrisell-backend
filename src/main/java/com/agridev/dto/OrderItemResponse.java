package com.agridev.dto;

import com.agridev.model.Product;
import lombok.Data;

// DTO class to send order item details in response
@Data
public class OrderItemResponse {

    private ProductItem product;

    private int quantity;

    private double price;

    private double lineTotal;

}
