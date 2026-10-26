package com.agridev.dto;

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
    private ParentCategoryDTO category;
    private ParentCategoryDTO parentCategory;
    private String imgUrl;
    private OfferSummaryDTO offer;
    private Double discountedPrice;
}
