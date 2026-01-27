package com.agridev.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class ProductItem {

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productName;
}
