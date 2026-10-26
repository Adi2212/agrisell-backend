package com.agridev.dto;

import lombok.Data;

@Data
public class CartSyncRequestItem {
    private Long id; // product id
    private int qty;
}
