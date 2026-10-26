package com.agridev.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSummaryDTO {
    private Long id;
    private String name;
    private Double price;
    private String imgUrl;
    private String categoryName;
    private String parentCategoryName;
}
