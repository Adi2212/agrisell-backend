package com.agridev.dto;

import com.agridev.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private int stockQuantity;
    private Category category;
    private String imageUrl;
    private Long userId;
    private String addedBy;
    private String addedByEmail;
}
