package com.agridev.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class AddProductDTO {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 to 100 characters")
    private String name;

    @NotBlank(message = "Product description is required")
    @Size(min = 5, max = 500, message = "Description must be between 5 to 500 characters")
    private String description;

    @NotNull(message = "Product price is required")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Image URL is required")
    private String imgUrl;

    @Min(value = 1, message = "Stock quantity must be at least 1")
    private int stockQuantity;
}
