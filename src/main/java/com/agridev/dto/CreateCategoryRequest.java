package com.agridev.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Image URL is required")
    private String imgUrl;


    private Long parentId;
}
