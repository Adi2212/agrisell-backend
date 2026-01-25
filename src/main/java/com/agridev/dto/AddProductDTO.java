package com.agridev.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class AddProductDTO {
    private String name;
    private String description;
    private Double price;
    private Long categoryId;
    private String imgUrl;
    private int stockQuantity;
}
