package com.agridev.dto;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    private String name;
    private String imageUrl;
    private Long parentId;
}
